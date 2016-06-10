package actors

import akka.actor._
import models.operations
import services.ImageTransformer

object WorkExecutor {
  def props = Props(new WorkExecutor)
}

class WorkExecutor extends WorkExecutorActor {
  override def receive: Receive = {
    case operations.Resize(imageData, width, height) =>
      val result = ImageTransformer.resizeImageTo(imageData, width, height)
      completeWork(result)
  }
}
