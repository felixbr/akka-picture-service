package actors.systems

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Server}
import com.typesafe.config.ConfigFactory
import helpers.Config

object ServerActorSystem {
  private val port = Config.env match {
    case "test" => 0
    case _      => 2551
  }

  private val config = ConfigFactory
    .parseString(
      s"""akka.remote.netty.tcp.port = $port
         |akka.cluster.seed-nodes = [
         |  "akka.tcp://api-server@127.0.0.1:$port"
         |]
       """.stripMargin)
    .withFallback(Config.config)

  val system = ActorSystem("api-server", config)
  val materializer = ActorMaterializer()(system)
  val executionContext = system.dispatcher
}

trait ServerActorSystem {
  implicit val system = ServerActorSystem.system
  implicit val materializer = ServerActorSystem.materializer
  implicit val executionContext = ServerActorSystem.executionContext
}
