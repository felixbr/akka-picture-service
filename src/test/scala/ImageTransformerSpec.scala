import java.io.File

import akka.stream.scaladsl._
import com.sksamuel.scrimage.Image
import org.scalatest._
import services.{ImageTransformer, ServerActorSystem}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source

class ImageTransformerSpec extends WordSpec with Matchers with ServerActorSystem {

  val testFile = new File(getClass.getResource("/wallpaper.jpg").getPath)
  val imageData = Await.result(
    FileIO.fromFile(testFile).runFold(Array.empty[Byte]) { (a, b) => a ++ b.toArray[Byte] },
    3.seconds
  )

  println(testFile.getAbsolutePath)

  "The ImageTransformer" should {
    "resize an image to 1024x768" in {
      val result = ImageTransformer.resizeImageTo(imageData, 1024, 768)

      println(result.absoluteImagePath)

      Image(result.imageData).width shouldEqual 1024
      Image(result.imageData).height shouldEqual 768
    }
  }
}
