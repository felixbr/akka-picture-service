import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import api.routes.apiRoute
import org.scalatest.{Matchers, WordSpec}

class ImageUploadSpec extends WordSpec with Matchers with ScalatestRouteTest {
  val multipartForm =
    Multipart.FormData(Multipart.FormData.BodyPart.Strict(
      "image",
      HttpEntity(ContentTypes.`text/plain(UTF-8)`, "2,3,5\n7,11,13,17,23\n29,31,37\n")))

  "The ImageUpload" should {
    "succeed" in {
      Post("/images", multipartForm) ~> apiRoute ~> check {
        status shouldEqual StatusCodes.Created
      }
    }
  }

}
