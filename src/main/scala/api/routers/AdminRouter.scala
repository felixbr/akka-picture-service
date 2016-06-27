package api.routers

import actors.systems.ServerActorSystem
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._

object AdminRouter extends ServerActorSystem {

  val routes: Route =
    path("ws") {
      handleWebSocketMessages(admin.live.wsHandlerFlow)
    }
}
