package actors

import actors.ImageProcessingWorker.answers._
import actors.ImageProcessingWorker.messages._
import akka.actor._
import models.core.ImageData
import services.{ImageTransformer, ProcessedImage}

object ImageProcessingWorker {
  val supervisorStrategy = OneForOneStrategy() {
    case _ => SupervisorStrategy.Restart
  }

  def props = Props[ImageProcessingWorker]

  object messages {
    case class Resize(imageData: ImageData, width: Int, height: Int)
  }

  object answers {
    case class WorkAcknowledged()
    case class WorkDone(processedImage: ProcessedImage)
  }
}

class ImageProcessingWorker extends Actor {
  override def receive: Receive = {
    case Resize(imageData, width, height) =>
      sender() ! WorkAcknowledged
      val processedImage = ImageTransformer.resizeImageTo(imageData, width, height)
      sender() ! WorkDone(processedImage)

    case x =>
      throw new Exception(s"Unrecognized message: $x")
  }
}
