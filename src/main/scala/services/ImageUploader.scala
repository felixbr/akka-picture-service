package services

import actors.systems.ServerActorSystem
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import helpers.Config
import models.core._

import scala.concurrent.Future

object ImageUploader extends ServerActorSystem {
  def saveFileOnDisk(bytesSource: Source[ByteString, Any], fileName: FileName): Future[FileName] = {
    val outFile = (Config.directories.uploadedImages/fileName).toIO
    val sink = FileIO.toFile(outFile)

    bytesSource.runWith(sink).map(x => outFile.getName)
  }
}
