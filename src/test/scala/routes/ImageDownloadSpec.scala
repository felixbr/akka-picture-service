package routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpEncodings._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.testkit._
import ammonite.ops._
import api.routes.apiRoute
import helpers.Config
import models.core._
import org.scalatest._
import org.scalatest.matchers.Matcher

class ImageDownloadSpec extends WordSpec with Matchers with ScalatestRouteTest {
  val fileName = "black_mage_cat_100.jpg"
  val imageData = read.bytes(resource/fileName)

  def setupUploadedImage(fileName: String): Unit = {
    write.over(Config.directories.uploadedImages/fileName, imageData)
  }

  def haveContentEncoding(encoding: HttpEncoding): Matcher[HttpResponse] = {
    be(encoding) compose { (_: HttpResponse).header[`Content-Encoding`].map(_.encodings.head).getOrElse(HttpEncodings.identity) }
  }

  "/api/images/{fileName}" when {

    "retrieving an existing image" should {
      setupUploadedImage(fileName)

      "return 200 OK" in {
        Get(s"/api/images/$fileName") ~> apiRoute ~> check {
          status shouldEqual StatusCodes.OK
        }
      }

      "return the original image" in {
        Get(s"/api/images/$fileName") ~> apiRoute ~> check {
          responseAs[ImageData] shouldEqual imageData
        }
      }

      "use gzip compression" ignore {
        Get(s"/api/images/$fileName")
          .withHeaders(`Accept-Encoding`(HttpEncodings.gzip)) ~> apiRoute ~> check {

          response should haveContentEncoding(gzip)
        }
      }
    }

    "retrieving a missing image" should {
      "return 404 Not Found" in {
        Get(s"/api/images/doesnt_exist") ~> apiRoute ~> check {
          status shouldEqual StatusCodes.NotFound
        }
      }
    }
  }
}
