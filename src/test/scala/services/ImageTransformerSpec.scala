package services

import ammonite.ops._
import com.sksamuel.scrimage.Image
import org.scalatest._

class ImageTransformerSpec extends WordSpec with Matchers with ServerActorSystem {
  val imageData = read.bytes(resource/"wallpaper.jpg")

  "The ImageTransformer" should {
    "resize an image to 1024x768" in {
      val result = ImageTransformer.resizeImageTo(imageData, 1024, 768)

      Image(result.imageData).width shouldEqual 1024
      Image(result.imageData).height shouldEqual 768
    }
  }
}
