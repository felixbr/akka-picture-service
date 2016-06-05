package models

object core {
  type FileName = String
  type ImageData = Array[Byte]

  case class WorkerId(value: String) extends AnyVal
  case class WorkId(value: String) extends AnyVal

  case class Work[A](workId: WorkId, workData: A)
}
