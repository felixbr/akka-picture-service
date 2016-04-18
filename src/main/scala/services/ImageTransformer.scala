package services

import java.io.File
import java.util.UUID

import com.sksamuel.scrimage._
import com.sksamuel.scrimage.nio.JpegWriter

case class ProcessedImage(imageData: Array[Byte], absoluteImagePath: String)

object ImageTransformer {
  implicit val writer = JpegWriter.NoCompression

  def resizeImageTo(imageData: Array[Byte], width: Int, height: Int): ProcessedImage = {
    val transformedImage = Image(imageData).cover(width, height)
    val transformedImageData = transformedImage.bytes

    val imageFilePath = s"processed_images/${UUID.nameUUIDFromBytes(transformedImageData)}.jpg"
    val outputFile = new File(imageFilePath)

    transformedImage.output(outputFile)

    ProcessedImage(transformedImageData, imageFilePath)
  }
}
