package actors

import actors.WorkManager.WorkerState
import actors.WorkManagerMonitor.messages.{WorkMonitorMessage, WorkStateUpdate}
import akka.actor.{Actor, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import models.WorkState
import models.core.WorkerId

import scala.concurrent.duration._

object WorkManagerMonitor {
  def props = Props(new WorkManagerMonitor)

  def topic = "WorkManagerMonitor"

  object messages {
    sealed trait WorkMonitorMessage

    case class WorkStateUpdate(
      newState: WorkState,
      timestamp: Long = System.currentTimeMillis()
    ) extends WorkMonitorMessage

    case class WorkersUpdate(
      newWorkers: Map[WorkerId, WorkerState],
      timestamp: Long = System.currentTimeMillis()
    ) extends WorkMonitorMessage
  }
}

class WorkManagerMonitor extends Actor {
  val mediator = DistributedPubSub(context.system).mediator

  def receive: Receive = {
    case m: WorkStateUpdate =>
      mediator ! Publish(WorkManagerMonitor.topic, m)

    case m: WorkMonitorMessage =>
      mediator ! Publish(WorkManagerMonitor.topic, m)
  }
}
