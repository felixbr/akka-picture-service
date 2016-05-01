package models

object exceptions {
  case class FileNotFound(path: String) extends Exception(s"File not found: $path")
}
