package routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpEncodings._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.testkit._
import api.routes.apiRoute
import models.core._
import org.scalatest._
import testutil.matchers._
import testutil.scopes._

class ImageDownloadSpec extends WordSpec with Matchers with ScalatestRouteTest
  with ParallelTestExecution {

  "/api/images/{fileName}" when {
    "retrieving an existing image" should {
      "return 200 OK" in withUploadedImage { fileName =>
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
