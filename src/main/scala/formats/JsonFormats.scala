package formats

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import models.responses._

trait JsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val errorResponseFormat = jsonFormat1(ErrorResponse)
}
