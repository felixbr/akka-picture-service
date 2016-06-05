package api

import actors.systems.ServerActorSystem
import akka.http.scaladsl.Http
import api.routes._

import scala.io.StdIn

object Server extends App with ServerActorSystem {

  val bindingFuture = Http().bindAndHandle(apiRoute, "localhost", 3000)

  println(s"Server online at http://localhost:3000/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.terminate()) // and shutdown when done

}
