package routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import ammonite.ops._
import api.routes.apiRoute
import helpers.Config
import models.core._
import org.scalatest._
import org.scalatest.tagobjects.Slow

import scala.concurrent.duration._

class ImagesTransformationsSpec extends WordSpec with Matchers with ScalatestRouteTest {
  val fileName = "wallpaper.jpg"
  val imageData = read.bytes(resource/fileName)
  val processedFileName = "wallpaper_resized.jpg"
  val processedImageData = read.bytes(resource/processedFileName)

  def setupUploadedImage(fileName: String): Unit = {
    write.over(Config.uploadedImagesDirectory/fileName, imageData)
  }

  implicit val routeTimeout = RouteTestTimeout(5.seconds)

  "/api/images/{fileName}/resize/{width}/{height}" when {

    "uploaded image with fileName exists" should {
      setupUploadedImage(fileName)

      "return a correct image response" taggedAs Slow in {
        Get(s"/api/images/$fileName/resize/1024/768") ~> apiRoute ~> check {
          status shouldEqual StatusCodes.OK
          mediaType shouldEqual MediaTypes.`image/jpeg`

          responseAs[ImageData] shouldEqual processedImageData
        }
      }
    }
  }
}
