import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import helpers.FileHelper
import org.scalatest._
import api.routes.apiRoute
import ammonite.ops._

class ImageDownloadSpec extends FreeSpec with Matchers with ScalatestRouteTest {
  def withUploadedImage(fileName: String)(block: => Unit): Unit = {
    val imageData = FileHelper.loadImageDataFromResources("/wallpaper.jpg")

    write.over(cwd/'uploaded_images/fileName, imageData)
  }

  "/images/{fileName}" - {

    "when retrieving an existing image" - {
      val fileName = "wallpaper.jpg"
      withUploadedImage(fileName) {
        "returns 200 OK" - {
          Get(s"/images/$fileName") ~> apiRoute ~> check {
            status shouldEqual StatusCodes.OK
          }
        }
      }
    }

    "when retrieving a missing image" - {
      "returns 404 Not Found" ignore {
        Get(s"/images/doesn't_exist") ~> apiRoute ~> check {
          status shouldEqual StatusCodes.NotFound
        }
      }
    }
  }
}
