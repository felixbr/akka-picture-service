package helpers

import actors.systems.ServerActorSystem
import ammonite.ops._
import models.core._

import scala.concurrent.Future

object FileHelper extends ServerActorSystem {
  def loadImageDataFromUploads(fileName: FileName): Future[ImageData] = Future {
    read.bytes(Config.directories.uploadedImages/fileName)
  }

  def loadImageDataFromProcessed(fileName: FileName): Future[ImageData] = Future {
    read.bytes(Config.directories.processedImages/fileName)
  }
}
