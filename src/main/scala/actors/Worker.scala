package actors

import java.util.UUID

import actors.Worker.messages._
import actors.WorkManager.answers.WorkIsReady
import actors.WorkManager.messages.{RequestWork, WorkIsDone}
import actors.Worker._
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import models.core._
import services.ProcessedImage

import scala.concurrent.duration._

import scala.language.existentials

object Worker {
  val supervisorStrategy = OneForOneStrategy() {
    case _ => SupervisorStrategy.Restart
  }

  def props(workManager: ActorRef, registerInterval: FiniteDuration = 5.seconds) = {
    Props(new Worker(workManager, registerInterval))
  }

  sealed trait State
  case object Idle extends State
  case object Working extends State
  case object WaitingForWorkIsDoneAck extends State

  sealed trait Data
  case object NoWorkData extends Data
  case class WorkData(currentWork: Work[_]) extends Data
  case class ResultData[Result](currentResult: Result, workId: WorkId) extends Data

  object messages {
    case class WorkCompleted[Result](result: Result)
  }

  object answers {
    case class WorkAcknowledged()
  }
}

class Worker(val workManager: ActorRef, val registerInterval: FiniteDuration)
  extends FSM[State, Data] with ActorLogging with WorkerRegistering with WorkExecutorChild {

  lazy val workerId = WorkerId(UUID.randomUUID().toString)

  startWith(Idle, NoWorkData)

  when(Idle) {
    case Event(WorkIsReady, data) =>
      sendToWorkManager(RequestWork(workerId))
      stay using data


    case Event(w @ Work(workId, workData), NoWorkData) =>
      log.info(s"Worker ${workerId.value} received new work: ${workId.value}")
      workExecutor ! workData
      goto(Working) using WorkData(w)
  }

  when(Working) {
    case Event(WorkCompleted(result: ProcessedImage), WorkData(currentWork)) =>
      val currentWorkId = currentWork.workId
      log.info(s"Work $currentWorkId is complete. Result: $result")
      sendToWorkManager(WorkIsDone(workerId, currentWorkId, result))
      goto(WaitingForWorkIsDoneAck) using ResultData(result, currentWorkId)

    case Event(a, data) =>
      log.error(s"Worker ${workerId.value} received $a while working")
      stay using data
  }

  when(WaitingForWorkIsDoneAck, stateTimeout = 5.seconds) {
    case Event(WorkManager.answers.WorkIsDoneAck(workId), data @ ResultData(_, resultWorkId))
      if workId == resultWorkId =>

      sendToWorkManager(RequestWork(workerId))
      goto(Idle) using NoWorkData

    case Event(StateTimeout, data @ ResultData(result, resultWorkId)) =>
      log.info("No ack from manager, retrying")
      sendToWorkManager(WorkIsDone(workerId, resultWorkId, result))
      stay using data
  }

  whenUnhandled {
    case Event(Terminated(`workExecutor`), data) =>
      stop()

    case Event(WorkIsReady, data) =>
      stay using data

    case Event(e, s) =>
      log.warning(s"Worker ${workerId.value} received unrecognized message: $e in state: $stateName/$s")
      stay using s
  }

  def sendToWorkManager(msg: Any): Unit = {
    workManager ! msg
  }
}

trait WorkerRegistering { self: Worker =>
  def workManager: ActorRef
  def registerInterval: FiniteDuration
  def workerId: WorkerId

  implicit val dispatcher = context.dispatcher

  val registerTask = context.system.scheduler.schedule(
    initialDelay = 0.seconds,
    interval = registerInterval,
    receiver = workManager,
    message = WorkManager.messages.RegisterWorker(workerId)
  )

  override def postStop(): Unit = registerTask.cancel()
}

trait WorkExecutorChild { self: Worker =>
  def workerId: WorkerId
  def workManager: ActorRef

  val workExecutor = context.watch(context.actorOf(WorkExecutor.props, "exec"))

  override def supervisorStrategy = OneForOneStrategy() {
    case _: ActorInitializationException => Stop
    case _: DeathPactException           => Stop
    case _: Exception =>
      val currentWorkId = stateData match {
        case WorkData(Work(workId, _)) => workId
        case _ => WorkId("no current work")
      }
      workManager ! WorkManager.messages.WorkFailed(workerId, currentWorkId)
      Restart
  }
}
