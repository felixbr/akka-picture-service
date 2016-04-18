package services

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object ServerActorSystem {
  val system = ActorSystem("api-server")
  val materializer = ActorMaterializer()(system)
  val executionContext = system.dispatcher
}

trait ServerActorSystem {
  implicit val system = ServerActorSystem.system
  implicit val materializer = ServerActorSystem.materializer
  implicit val executionContext = ServerActorSystem.executionContext
}
