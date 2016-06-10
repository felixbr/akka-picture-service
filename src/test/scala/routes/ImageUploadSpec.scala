package routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Source
import akka.util.ByteString
import ammonite.ops._
import api.routes.apiRoute
import helpers.Config
import models.core._
import org.scalatest.{Matchers, WordSpec}
import testutil.fixtures.TestImageFixture

trait MultipartFormFixture extends TestImageFixture {
  val fileSource = Source(List(ByteString.fromArray(imageData)))

  val multipartForm =
    Multipart.FormData(
      Multipart.FormData.BodyPart(
        "image",
        HttpEntity(MediaTypes.`image/jpeg`, imageData.length.toLong, fileSource),
        Map("fileName" -> "black_mage_cat_100.jpg")
      )
    )
}

class ImageUploadSpec extends WordSpec with Matchers with ScalatestRouteTest {

  "The ImageUpload" should {
    "succeed" in new MultipartFormFixture {
      Post("/api/images", multipartForm) ~> apiRoute ~> check {
        status shouldEqual StatusCodes.Created

        exists(Config.directories.uploadedImages/responseAs[FileName]) shouldEqual true
        read.bytes(Config.directories.uploadedImages / responseAs[FileName]).length shouldEqual imageData.length
      }
    }
  }
}
