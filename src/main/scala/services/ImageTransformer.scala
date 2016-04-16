package services

import java.io.File
import java.util.UUID

import com.sksamuel.scrimage._
import com.sksamuel.scrimage.nio.JpegWriter

case class ProcessedImage(imageData: Array[Byte], absoluteImagePath: String)

object ImageTransformer {
  implicit val writer = JpegWriter.NoCompression

  def resizeImageTo(imageData: Array[Byte], width: Int, height: Int): ProcessedImage = {
    val convertedImage = Image(imageData).cover(width, height)
    val convertedImageData = convertedImage.bytes

    val imageFilePath = s"processed_images/${UUID.nameUUIDFromBytes(convertedImageData)}.jpg"

    val outputFile = new File(imageFilePath)

    convertedImage.output(outputFile)

    ProcessedImage(convertedImageData, imageFilePath)
  }
}
