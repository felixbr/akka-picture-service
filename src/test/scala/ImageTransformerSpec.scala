import com.sksamuel.scrimage.Image
import helpers.FileHelper
import org.scalatest._
import services.{ImageTransformer, ServerActorSystem}

class ImageTransformerSpec extends WordSpec with Matchers with ServerActorSystem {

  val imageData = FileHelper.loadImageDataFromResources("/wallpaper.jpg")

  "The ImageTransformer" should {
    "resize an image to 1024x768" in {
      val result = ImageTransformer.resizeImageTo(imageData, 1024, 768)

      Image(result.imageData).width shouldEqual 1024
      Image(result.imageData).height shouldEqual 768
    }
  }
}
