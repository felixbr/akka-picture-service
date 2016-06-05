package api

import actors.systems.ServerActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import exceptionHandlers._
import api.routers.{ImagesRouter, SwaggerRouter}

object routes extends ServerActorSystem {

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
      }
    }
}
