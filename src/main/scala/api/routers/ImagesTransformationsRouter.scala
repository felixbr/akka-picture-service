package api.routers

import java.nio.file.NoSuchFileException

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import helpers.FileHelper
import services.{ImageTransformer, ServerActorSystem}

import scala.util.{Failure, Success}

object ImagesTransformationsRouter extends ServerActorSystem {
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

                case Failure(e: NoSuchFileException) =>
                  complete(HttpResponse(
                    status = StatusCodes.NotFound,
                    entity = s"Uploaded file '$fileName' not found"
                  ))

                case Failure(e) => throw e
              }
            }
          }
        }
      }
    }
}
