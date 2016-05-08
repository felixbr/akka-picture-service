package api.routers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

object SwaggerRouter {

  val routes: Route =
    pathPrefix("swagger") {
      get {
        pathEndOrSingleSlash {
          getFromFile("public/swagger/index.html")
        } ~
        pathPrefix("api-docs") {
          getFromFile("public/swagger.yml")
        } ~
        getFromBrowseableDirectory("public/swagger")
      }
    }

}
