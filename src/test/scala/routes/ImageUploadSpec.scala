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

class ImageUploadSpec extends WordSpec with Matchers with ScalatestRouteTest {
  val imageData = read.bytes(resource/"wallpaper.jpg")
  val fileSource = Source(List(ByteString.fromArray(imageData)))

  val multipartForm =
    Multipart.FormData(
      Multipart.FormData.BodyPart(
        "image",
        HttpEntity(MediaTypes.`image/jpeg`, imageData.length.toLong, fileSource),
        Map("fileName" -> "wallpaper.jpg")
      )
    )

  "The ImageUpload" should {
    "succeed" in {
      Post("/api/images", multipartForm) ~> apiRoute ~> check {
        status shouldEqual StatusCodes.Created

        exists(Config.uploadedImagesDirectory/responseAs[FileName]) shouldEqual true
        read.bytes(Config.uploadedImagesDirectory/responseAs[FileName]).size shouldEqual imageData.size
      }
    }
  }
}
