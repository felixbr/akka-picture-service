package actors.systems

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object WorkerActorSystem {
  val system = ActorSystem("worker-server")
  val materializer = ActorMaterializer()(system)
  val executionContext = system.dispatcher
}

trait WorkerActorSystem {
  implicit val system = WorkerActorSystem.system
  implicit val materializer = WorkerActorSystem.materializer
  implicit val executionContext = WorkerActorSystem.executionContext
}
