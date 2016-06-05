package api.routers

import java.nio.file.NoSuchFileException

import actors.systems.ServerActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import models.responses._
import formats.JsonFormats
import helpers.FileHelper
import services.ImageTransformer

import scala.util.{Failure, Success}

object ImagesTransformationsRouter extends ServerActorSystem with JsonFormats {

  val routes: Route =
    get {
      pathPrefix(Segment) { fileName =>
        pathPrefix("resize") {
          pathPrefix(IntNumber) { width =>
            pathPrefix(IntNumber) { height =>
              val processedImage = FileHelper.loadImageDataFromUploads(fileName)
                .map(image => ImageTransformer.resizeImageTo(image, width, height))

              onComplete(processedImage) {
                case Success(image) =>
                  complete(HttpResponse(
                    entity = HttpEntity(MediaTypes.`image/jpeg`, image.imageData)
                  ))

                case Failure(_: NoSuchFileException) =>
                  complete((StatusCodes.NotFound, ErrorResponse(s"File '$fileName' not found")))

                case Failure(e: IllegalArgumentException) =>
                  complete((StatusCodes.BadRequest, ErrorResponse(e.getMessage)))

                case Failure(e) => throw e
              }
            }
          }
        }
      }
    }
}
