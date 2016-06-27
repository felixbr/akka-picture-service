package actors.systems

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import helpers.Config

object WorkerActorSystem {
  private val port = Config.env match {
    case "test" => 0
    case _      => 2601
  }

  private val config = ConfigFactory
    .parseString(
      s"""akka.remote.netty.tcp.port = $port
         |akka.cluster.seed-nodes = [
         |  "akka.tcp://api-server@127.0.0.1:2551"
         |]
       """.stripMargin)
    .withFallback(Config.config)

  val system = ActorSystem("api-server", config)
  val materializer = ActorMaterializer()(system)
  val executionContext = system.dispatcher
}

trait WorkerActorSystem {
  implicit val system = WorkerActorSystem.system
  implicit val materializer = WorkerActorSystem.materializer
  implicit val executionContext = WorkerActorSystem.executionContext
}
