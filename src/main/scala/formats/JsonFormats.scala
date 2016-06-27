package formats

import actors.WorkManager.{WorkerBusy, WorkerIdle, WorkerStatus}
import admin.clientServerProtocol.toClient.{CurrentWorkerState, WorkerInfo}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import models.core._
import spray.json._
import models.responses._

import scala.concurrent.duration._

trait JsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val errorResponseFormat = jsonFormat1(ErrorResponse)

//  implicit object WorkerIdFormat extends JsonFormat[WorkerId] {
//    def write(workerId: WorkerId) = JsString(workerId.value)
//
//    def read(v: JsValue) = WorkerId(v.asInstanceOf[JsString].value)
//  }
//
//  implicit object WorkerStatusFormat extends JsonFormat[WorkerStatus] {
//    def write(ws: WorkerStatus) = ws match {
//      case WorkerIdle =>
//        JsObject(
//          "state" -> JsString("WorkerIdle")
//        )
//
//      case WorkerBusy(workId, deadline) =>
//        JsObject(
//          "state" -> JsString("WorkerBusy"),
//          "workId" -> JsString(workId.value),
//          "deadline" -> JsNumber(deadline.time.toMillis)
//        )
//    }
//
//    def read(v: JsValue): WorkerStatus = {
//      v.asJsObject.getFields("state", "workId", "deadline") match {
//        case Seq(JsString("WorkBusy"), JsString(workId), JsNumber(deadline)) =>
//          WorkerBusy(WorkId(workId), Deadline(deadline.toLong.milliseconds))
//
//        case Seq(JsString("WorkIdle"), _, _) =>
//          WorkerIdle
//
//        case _ => throw new DeserializationException(s"WorkerStatus expected, $v found!")
//      }
//    }
//  }
//
//  implicit val workerInfoFormat = jsonFormat2(WorkerInfo)
//
//  implicit val currentWorkerStateFormat = jsonFormat1(CurrentWorkerState)
}
