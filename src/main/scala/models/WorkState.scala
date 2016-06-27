package models

import models.core._

import scala.collection.immutable.Queue

import scala.language.existentials

object WorkState {

  def empty: WorkState = WorkState(
    pendingWork = Queue.empty,
    workInProgress = Map.empty,
    acceptedWorkIds = Set.empty,
    doneWorkIds = Set.empty)

  trait WorkDomainEvent
  case class WorkAccepted[A](work: Work[A]) extends WorkDomainEvent
  case class WorkStarted(workId: WorkId) extends WorkDomainEvent
  case class WorkCompleted(workId: WorkId, result: Any) extends WorkDomainEvent
  case class WorkerFailed(workId: WorkId) extends WorkDomainEvent
  case class WorkerTimedOut(workId: WorkId) extends WorkDomainEvent

}

case class WorkState (
  pendingWork: Queue[Work[_]],
  workInProgress: Map[WorkId, Work[_]],
  acceptedWorkIds: Set[WorkId],
  doneWorkIds: Set[WorkId]
) {

  import WorkState._

  def hasWork: Boolean = pendingWork.nonEmpty
  def nextWork: Work[_] = pendingWork.head
  def isAccepted(workId: WorkId): Boolean = acceptedWorkIds.contains(workId)
  def isInProgress(workId: WorkId): Boolean = workInProgress.contains(workId)
  def isDone(workId: WorkId): Boolean = doneWorkIds.contains(workId)
  def isPending(workId: WorkId): Boolean =
    pendingWork.exists { case Work(`workId`, _) => true }

  def updated(event: WorkDomainEvent): WorkState = event match {
    case WorkAccepted(work) ⇒
      copy(
        pendingWork = pendingWork enqueue work,
        acceptedWorkIds = acceptedWorkIds + work.workId)

    case WorkStarted(workId) ⇒
      val (work, rest) = pendingWork.dequeue
      require(workId == work.workId, s"WorkStarted expected workId $workId == ${work.workId}")
      copy(
        pendingWork = rest,
        workInProgress = workInProgress + (workId -> work))

    case WorkCompleted(workId, result) ⇒
      copy(
        workInProgress = workInProgress - workId,
        doneWorkIds = doneWorkIds + workId)

    case WorkerFailed(workId) ⇒
      copy(
        pendingWork = pendingWork enqueue workInProgress(workId),
        workInProgress = workInProgress - workId)

    case WorkerTimedOut(workId) ⇒
      copy(
        pendingWork = pendingWork enqueue workInProgress(workId),
        workInProgress = workInProgress - workId)
  }

  override def toString: String = {
    s"""Workstate(
       |  pendingWork:     $pendingWork
       |  workInProgress:  $workInProgress
       |  acceptedWorkIds: $acceptedWorkIds
       |  doneWorkIds:     $doneWorkIds
       |)
     """.stripMargin.trim
  }
}
