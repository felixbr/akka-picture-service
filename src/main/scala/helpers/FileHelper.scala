package helpers

import java.io.File

import akka.stream.scaladsl._
import akka.util.ByteString
import services.ServerActorSystem

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object FileHelper extends ServerActorSystem {
  def loadImageDataFromResources(resourceName: String): Array[Byte] = {
    Await.result(loadImageDateFromResourcesAsync(resourceName), 3.seconds)
  }

  def loadImageDateFromResourcesAsync(resourceName: String): Future[Array[Byte]] = {
    val testFile = new File(getClass.getResource("/wallpaper.jpg").getPath)

    FileIO.fromFile(testFile).runFold(Array.empty[Byte]) { (a, b) => a ++ b.toArray[Byte] }
  }
}
