package api.routers

import actors.systems.ServerActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import helpers.FileHelper
import services.ImageUploader

import scala.util.{Failure, Success}

object ImagesRouter extends ServerActorSystem {

  val routes: Route =
    pathEndOrSingleSlash {
      post {
        fileUpload("image") { case (metadata, byteSource) =>
          val result = ImageUploader.saveFileOnDisk(byteSource, metadata.fileName)

          onSuccess(result) { fileName =>
            complete(HttpResponse(status = StatusCodes.Created, entity = fileName))
          }
        }
      }
    } ~
    pathPrefix(Segment) { fileName =>
      pathEndOrSingleSlash {
        get {
          val imageDataFut = FileHelper.loadImageDataFromUploads(fileName)

          onComplete(imageDataFut) {
            case Success(imageData) =>
              complete(HttpResponse(
                entity = HttpEntity(MediaTypes.`image/jpeg`, imageData)
              ))

            case Failure(exc) =>
              complete(HttpResponse(status = StatusCodes.NotFound))
          }
        }
      }
    } ~
    ImagesTransformationsRouter.routes

}
