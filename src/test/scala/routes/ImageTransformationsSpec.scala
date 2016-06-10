package routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit._
import ammonite.ops._
import api.routes.apiRoute
import helpers.Config
import models.core._
import org.scalatest._
import org.scalatest.tagobjects.Slow
import testutil.fixtures._
import testutil.scopes._

import scala.concurrent.duration._

class ImageTransformationsSpec extends WordSpec with Matchers with ScalatestRouteTest
  with ParallelTestExecution {

  implicit val routeTimeout = RouteTestTimeout(10.seconds)

  "/api/images/{fileName}/resize/{width}/{height}" when {
    "uploaded image with fileName exists" should {
      "return a correct image response" taggedAs Slow in
        new TestImageFixture with TestProcessedImageFixture {
          withUploadedImage { fileName =>

            Get(s"/api/images/$fileName/resize/50/50") ~> apiRoute ~> check {
              status shouldEqual StatusCodes.OK
              mediaType shouldEqual MediaTypes.`image/jpeg`

              responseAs[ImageData] shouldEqual processedImageData
            }
          }
      }
    }

    "uploaded image with fileName doesn't exist" should {
      "return 404 Not Found status" in {
        Get("/api/images/doesnt_exist/resize/50/50") ~> apiRoute ~> check {
          status shouldEqual StatusCodes.NotFound
        }
      }
    }
  }
}
