package actors

import java.util.UUID

import actors.Worker.messages._
import actors.WorkManager.answers.WorkIsReady
import actors.WorkManager.messages.{RequestWork, WorkIsDone}
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import models.core._
import services.ProcessedImage

import scala.concurrent.duration._

object Worker {
  val supervisorStrategy = OneForOneStrategy() {
    case _ => SupervisorStrategy.Restart
  }

  def props(workManager: ActorRef, registerInterval: FiniteDuration = 5.seconds) = {
    Props(new Worker(workManager, registerInterval))
  }

  object messages {
    case class WorkCompleted[Result](result: Result)
  }

  object answers {
    case class WorkAcknowledged()
  }
}

class Worker(val workManager: ActorRef, val registerInterval: FiniteDuration)
  extends Actor with ActorLogging with WorkerRegistering with WorkExecutorChild {

  lazy val workerId = WorkerId(UUID.randomUUID().toString)

  private var currentWork: Option[Work[_]] = None
  def currentWorkId: WorkId = currentWork match {
    case Some(Work(workId, _)) =>
      workId
    case _ =>
      throw new IllegalStateException("No current work")
  }

  override def receive = idle

  def idle: Receive = {
    case WorkIsReady =>
      sendToWorkManager(RequestWork(workerId))


    case w @ Work(workId, workData) =>
      log.info(s"Worker ${workerId.value} received new work: ${workId.value}")
      currentWork = Some(w)
      workExecutor ! workData
      context.become(working)
  }

  def working: Receive = {
    case WorkCompleted(result: ProcessedImage) =>
      log.info(s"Work ${currentWorkId} is complete. Result: $result")
      sendToWorkManager(WorkIsDone(workerId, currentWorkId, result))
      context.setReceiveTimeout(5.seconds)
      context.become(waitForWorkIsDoneAck(result))

    case a =>
      log.error(s"Worker ${workerId.value} received $a while working")
  }

  def waitForWorkIsDoneAck(result: ProcessedImage): Receive = {
    case WorkManager.answers.WorkIsDoneAck(workId) if workId == currentWorkId =>
      sendToWorkManager(RequestWork(workerId))
      context.setReceiveTimeout(Duration.Undefined)
      context.become(idle)

    case ReceiveTimeout =>
      log.info("No ack from manager, retrying")
      sendToWorkManager(WorkIsDone(workerId, currentWorkId, result))
  }

  override def unhandled(message: Any): Unit = message match {
    case Terminated(`workExecutor`) =>
      context.stop(self)

    case WorkIsReady =>

    case _ =>
      log.warning(s"Worker ${workerId.value} received unrecognized message: $message")
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
  def currentWorkId: WorkId
  def workManager: ActorRef

  val workExecutor = context.watch(context.actorOf(WorkExecutor.props, "exec"))

  override def supervisorStrategy = OneForOneStrategy() {
    case _: ActorInitializationException => Stop
    case _: DeathPactException           => Stop
    case _: Exception =>
      workManager ! WorkManager.messages.WorkFailed(workerId, currentWorkId)
      context.become(idle)
      Restart
  }
}
