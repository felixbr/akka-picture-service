package services

import java.util.UUID

import com.sksamuel.scrimage._
import com.sksamuel.scrimage.nio.JpegWriter
import helpers.Config
import models.core._

case class ProcessedImage(imageData: ImageData, absoluteImagePath: String)

object ImageTransformer {
  implicit val writer = JpegWriter.NoCompression

  def resizeImageTo(imageData: ImageData, width: Int, height: Int): ProcessedImage = {
    require(width < Config.transformations.resize.widthLimit, "requested image width is too large")
    require(height < Config.transformations.resize.heightLimit, "requested image height is too large")

    val transformedImage = Image(imageData).cover(width, height)
    val transformedImageData = transformedImage.bytes

    val imageFilePath = Config.directories.processedImages/s"${UUID.nameUUIDFromBytes(transformedImageData)}.jpg"

    transformedImage.output(imageFilePath.toIO)

    ProcessedImage(transformedImageData, imageFilePath.toString())
  }
}
