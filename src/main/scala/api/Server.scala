package api

import actors.systems.{ServerActorSystem, WorkerActorSystem}
import actors.{WorkManager, Worker}
import akka.http.scaladsl.Http
import ammonite.ops._
import api.routes._
import models.operations

import scala.io.StdIn
import scala.language.dynamics

object BackgroundProcessing extends WorkerActorSystem {
  val workManager = system.actorOf(WorkManager.props)

  def startWorkers() = {
    (1 to 5).foreach { n =>
      system.actorOf(Worker.props(workManager), s"worker-$n")
    }
  }

  def stopWorkers() = {
    system.terminate()
  }
}

object Server extends App with ServerActorSystem {
  import BackgroundProcessing.{workManager, startWorkers, stopWorkers}

  val bindingFuture = Http().bindAndHandle(apiRoute, "localhost", 3000)

  startWorkers()

  val imageData = read.bytes(resource/"black_mage_cat_100.jpg")
  workManager ! WorkManager.messages.NewWork(operations.Resize(imageData, 50, 50))

  println(s"Server online at http://localhost:3000/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete { _ =>   // and shutdown when done
      stopWorkers()
      system.terminate()
    }

}
