package services

import com.sksamuel.scrimage.Image
import helpers.Config
import org.scalatest._
import testutil.fixtures.TestImageFixture

class ImageTransformerSpec extends WordSpec with Matchers
  with ParallelTestExecution {

  val widthLimit = Config.transformations.resize.widthLimit
  val heightLimit = Config.transformations.resize.heightLimit

  "The ImageTransformer" should {
    "resize an image to 1024x768" in new TestImageFixture {
      val result = ImageTransformer.resizeImageTo(imageData, 50, 50)

      Image(result.imageData).width shouldEqual 50
      Image(result.imageData).height shouldEqual 50
    }

    "reject width or height arguments which are too large" in new TestImageFixture {
      intercept[IllegalArgumentException] {
        ImageTransformer.resizeImageTo(imageData, widthLimit + 1, heightLimit + 1)
      }
    }
  }
}
