package models

object operations {

  sealed trait WorkOperation
  case class Resize(width: Int, height: Int) extends WorkOperation

}
