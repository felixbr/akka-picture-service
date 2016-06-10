package actors

import actors.WorkManager._
import actors.WorkManager.answers._
import actors.WorkManager.messages._
import akka.actor._
import models.WorkState
import models.WorkState.{WorkAccepted, WorkCompleted, WorkStarted, WorkerFailed}
import models.core._

import scala.concurrent.duration._

object WorkManager {
  def props = Props(new WorkManager(10.seconds))

  sealed trait WorkerStatus
  case object WorkerIdle extends WorkerStatus
  case class WorkerBusy(assignedWork: WorkId, deadline: Deadline) extends WorkerStatus
  case class WorkerState(ref: ActorRef, status: WorkerStatus)

  object messages {
    case class NewWork[Payload](payload: Payload)

    case class RegisterWorker(workerId: WorkerId)
    case class RequestWork(workerId: WorkerId)
    case class WorkIsDone[Result](workerId: WorkerId, workId: WorkId, result: Result)
    case class WorkFailed(workerId: WorkerId, workId: WorkId)
  }

  object answers {
    case class NewWorkAck(workId: WorkId)
    case class WorkIsDoneAck(workId: WorkId)

    case object WorkIsReady
  }
}

class WorkManager(
  workTimeout: FiniteDuration,
  initialState: WorkState = WorkState.empty,
  initialWorkers: Map[WorkerId, WorkerState] = Map.empty
) extends Actor with ActorLogging {

  var workState = initialState  // only prepopulated in tests
  var workers = initialWorkers  // only prepopulated in tests

  override def receive: Receive = {
    case RegisterWorker(workerId) =>
      if (workers.contains(workerId)) {
        // update ActorRef in case it changed
        workers += (workerId -> workers(workerId).copy(ref = sender()))
      } else {
        log.info(s"Worker registered: ${workerId.value}")
        workers += (workerId -> WorkerState(sender(), status = WorkerIdle))

      }
      if (workState.hasWork) {
        log.info(s"Sending registered worker ${workerId.value} WorkIsReady")
        sender() ! WorkIsReady
      }

    case RequestWork(workerId) =>
      log.debug(s"RECEIVED RequestWork from ${workerId.value}")
      if (workState.hasWork) {
        log.debug("HAS WORK")
        workers.get(workerId) match {
          case Some(s @ WorkerState(_, WorkerIdle)) =>
            val work = workState.nextWork
            workState = workState.updated(WorkStarted(work.workId))
            log.info(s"Giving worker ${workerId.value} some work ${work.workId.value}")
            workers += (workerId -> s.copy(status = WorkerBusy(work.workId, Deadline.now + workTimeout)))
            sender() ! work
          case s =>
            log.debug(s"Worker ${workerId.value} not found in registered workers")
        }
      } else {
        log.debug(s"NO WORK")
      }

    case WorkIsDone(workerId, workId, result) =>
      // idempotent
      if (workState.isDone(workId)) {
        log.info(s"Resending WorkIsDoneAck: ${workId.value}")
        sender() ! WorkIsDoneAck(workId)
      } else if (!workState.isInProgress(workId)) {
        log.info(s"Work ${workId.value} not in progress, reported as done by worker ${workerId.value}")
      } else {
        log.info(s"Work ${workId.value} was done by worker ${workerId.value}")
        changeWorkerToIdle(workerId, workId)
        workState = workState.updated(WorkCompleted(workId, result))
        log.debug(s"WorkIsDone result for ${workId.value}: $result")
        // TODO publish result via PubSub
        sender() ! WorkIsDoneAck(workId)
      }

    case WorkFailed(workerId, workId) =>
      if (workState.isInProgress(workId)) {
        log.info(s"Work ${workId.value} failed by worker ${workerId.value}")
        changeWorkerToIdle(workerId, workId)
        workState = workState.updated(WorkerFailed(workId))
        // TODO notifyWorkers()
        notifyWorkersAboutWorkReady()
      }

    case NewWork(payload) =>
      val workId = WorkId(payload.hashCode().toString)

      // idempotent
      if (workState.isAccepted(workId)) {
        log.info(s"Resending NewWorkAck: ${workId.value}")
        sender() ! NewWorkAck(workId)
      } else {
        log.info(s"Accepted work: ${workId.value}")
        workState = workState.updated(WorkAccepted(Work(workId, payload)))
        sender() ! NewWorkAck(workId)
        // TODO notify workers
        notifyWorkersAboutWorkReady()
      }

    // TODO clean up timeouts + retry

    case x =>
      throw new Exception(s"Unrecognized message: $x")
  }

  def notifyWorkersAboutWorkReady(): Unit = {
    if (workState.hasWork) {
      workers.foreach {
        case (_, WorkerState(ref, WorkerIdle)) =>
          ref ! WorkIsReady
        case _ =>
          // busy, don't bother
      }
    }
  }

  def changeWorkerToIdle(workerId: WorkerId, workId: WorkId): Unit = {
    workers.get(workerId) match {
      case Some(s @ WorkerState(_, WorkerBusy(`workId`, _))) ⇒
        workers += (workerId -> s.copy(status = WorkerIdle))
      case _ ⇒
        // ok, might happen after standby recovery, worker state is not persisted
    }
  }
}
