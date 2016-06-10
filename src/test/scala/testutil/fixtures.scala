package testutil

import ammonite.ops._

object fixtures {

  trait TestImageFixture {
    val fileName = "black_mage_cat_100.jpg"
    val imageData = read.bytes(resource/fileName)
  }

  trait TestProcessedImageFixture {
    val processedFileName = "black_mage_cat_50.jpg"
    val processedImageData = read.bytes(resource/processedFileName)
  }
}
