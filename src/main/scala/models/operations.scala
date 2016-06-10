package models

import models.core.ImageData

object operations {

  sealed trait WorkOperation
  case class Resize(imageData: ImageData, width: Int, height: Int) extends WorkOperation

}
