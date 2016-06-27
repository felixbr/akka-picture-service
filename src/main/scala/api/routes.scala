package api

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import api.exceptionHandlers._
import api.routers.{AdminRouter, ImagesRouter, SwaggerRouter}

object routes {

  val apiRoute: Route =
    encodeResponse {
      pathSingleSlash {
        get {
          redirect("/swagger", StatusCodes.TemporaryRedirect)
        }
      } ~
      SwaggerRouter.routes ~
      pathPrefix("api") {
        handleExceptions(defaultHandler) {
          pathPrefix("images") {
            ImagesRouter.routes
          }
        }
      } ~
      pathPrefix("admin") {
        AdminRouter.routes
      }
    }
}
