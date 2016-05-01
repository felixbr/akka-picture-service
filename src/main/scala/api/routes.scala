package api

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import api.responses.exceptionHandlers._
import api.routers.ImagesRouter
import services.ServerActorSystem

object routes extends ServerActorSystem {

  val apiRoute: Route =
    encodeResponse {
      pathSingleSlash {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      } ~
      pathPrefix("api") {
        handleExceptions(defaultHandler) {
          pathPrefix("images") {
            ImagesRouter.routes
          }
        }
      }
    }
}
