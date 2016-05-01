package api.responses

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import models.exceptions._

object exceptionHandlers {
  val defaultHandler = ExceptionHandler {
    case FileNotFound(path) =>
      complete(HttpResponse(StatusCodes.NotFound, entity = s"Internal file '$path' not found!"))
  }
}
