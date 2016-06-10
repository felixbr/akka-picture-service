package actors

import actors.Worker.messages.WorkCompleted
import akka.actor.{Actor, ActorLogging}

trait WorkExecutorActor extends Actor with ActorLogging {
  def completeWork[Result](result: Result): Unit = {
    sender() ! WorkCompleted(result)
  }

  override def unhandled(message: Any) = {
    log.warning(s"WorkExecutor received unrecognized message: $message")
  }
}
