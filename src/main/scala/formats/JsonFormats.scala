package formats

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import models.responses._
import spray.json._

trait JsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val errorResponseFormat = jsonFormat1(ErrorResponse)
}
