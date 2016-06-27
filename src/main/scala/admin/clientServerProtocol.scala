package admin

import actors.WorkManager._
import actors.WorkManagerMonitor.messages._

object clientServerProtocol {
  object fromClient {
    sealed trait FromClientMessage
  }

  object toClient {
    sealed trait ToClientMessage

    case class WorkerInfo(workerId: String, state: WorkerStatus)
    case class CurrentWorkerState(
      workers: List[WorkerInfo],
      timestamp: Long
    ) extends ToClientMessage

    case class CurrentWorkState(
      pendingWork: List[String],
      activeWork: List[String],
      doneWork: List[String],
      timestamp: Long
    ) extends ToClientMessage

    val toClientMessage: PartialFunction[Any, ToClientMessage] = {
      case WorkersUpdate(newWorkers, timestamp) =>
        val workers = newWorkers.map {
          case (workerId, workerState) => WorkerInfo(workerId.value, workerState.status)
        }.toList
        CurrentWorkerState(workers, timestamp)

      case WorkStateUpdate(ws, timestamp) =>
        CurrentWorkState(
          pendingWork = ws.pendingWork.map(_.workId.value).toList,
          activeWork = ws.workInProgress.keys.map(_.value).toList,
          doneWork = ws.doneWorkIds.map(_.value).toList,
          timestamp = timestamp
        )
    }
  }
}