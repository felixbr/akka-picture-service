package routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit._
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
    write.over(Config.directories.uploadedImages/fileName, imageData)
  }

  implicit val routeTimeout = RouteTestTimeout(10.seconds)

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

    "uploaded image with fileName doesn't exist" should {

      "return 404 Not Found status" in {
        Get("/api/images/doesnt_exist/resize/1024/768") ~> apiRoute ~> check {
          status shouldEqual StatusCodes.NotFound
        }
      }
    }
  }
}
