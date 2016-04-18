package api

import java.io.File

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl._
import helpers.FileHelper
import services.ServerActorSystem

object routes extends ServerActorSystem {

  val apiRoute =
    pathSingleSlash {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    } ~
    path("images") {
      post {
        extractRequest { request =>
          val source = request.entity.dataBytes
          val outFile = new File("/tmp/outfile.dat")
          val sink = FileIO.toFile(outFile)
          val repl = source.runWith(sink).map(x => outFile.getName)

          onSuccess(repl) { repl =>
            complete(HttpResponse(status = StatusCodes.Created, entity = repl))
          }
        }
      } ~
      path(Segment) { fileName =>
        path(IntNumber) { width =>
          path(IntNumber) { height =>
            val imageDataFut = FileHelper.loadImageDateFromResourcesAsync(s"processed_images/$fileName")

            onSuccess(imageDataFut) { imageData => complete(HttpResponse(entity = imageData)) }
          }
        }
      }
    }
}
