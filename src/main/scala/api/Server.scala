package api

import actors.systems.{ServerActorSystem, WorkerActorSystem}
import actors.{WorkManager, WorkManagerMonitor, WorkSpammerActor, Worker}
import akka.http.scaladsl.Http
import api.routes._

import scala.io.StdIn

object BackgroundProcessing extends WorkerActorSystem {
  val workManagerMonitor = system.actorOf(WorkManagerMonitor.props)

  val workManager = system.actorOf(WorkManager.props(monitor = Some(workManagerMonitor)))

  def startWorkers() = {
    (1 to 5).foreach { n =>
      system.actorOf(Worker.props(workManager), s"worker-$n")
    }
  }

  def stopWorkers() = {
    system.terminate()
  }

  def startWorkSpam() = {
    system.actorOf(WorkSpammerActor.props(workManager))
  }
}

object Server extends App with ServerActorSystem {
  import BackgroundProcessing.{startWorkers, stopWorkers, startWorkSpam}

  val bindingFuture = Http().bindAndHandle(apiRoute, "localhost", 3000)

  startWorkers()
  startWorkSpam()

  println(s"Server online at http://localhost:3000/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind())                  // trigger unbinding from the port
    .flatMap(_ => stopWorkers())          // shutdown worker system
    .onComplete(_ => system.terminate())  // and shutdown when done

}
