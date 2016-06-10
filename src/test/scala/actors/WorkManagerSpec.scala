package actors

import actors.WorkManager._
import actors.systems.WorkerActorSystem
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import ammonite.ops._
import models.WorkState._
import models.core._
import models.{WorkState, operations}
import org.scalatest._
import services.ProcessedImage

import scala.concurrent.duration._

trait TestImageFixture {
  val fileName = "black_mage_cat_100.jpg"
  val imageData = read.bytes(resource/fileName)
}

class WorkManagerFixture(
  initialState: WorkState = WorkState.empty,
  initialWorkers: Map[WorkerId, WorkerState] = Map.empty
)(implicit system: ActorSystem) {

  val props = Props(new WorkManager(10.seconds, initialState, initialWorkers))
  val workManager = TestActorRef[WorkManager](props)
  val underlyingActor: WorkManager = workManager.underlyingActor
}

object WorkManagerSpec extends WorkerActorSystem

class WorkManagerSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers
  with BeforeAndAfterEach with BeforeAndAfterAll {

  def this() = this(WorkManagerSpec.system)

  implicit val workerId = WorkerId("123")

  def withRegisteredWorker(workerManager: TestActorRef[WorkManager])(testCode: => Unit) = {
    ignoreMsg { case answers.WorkIsReady => true }
    workerManager ! messages.RegisterWorker(workerId)

    testCode
  }

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  "The manager actor" when {
    "a worker registers itself" when {
      "there is no work" should {
        "accept the register" in new WorkManagerFixture {
          workManager ! messages.RegisterWorker(workerId)
          expectNoMsg
        }
      }

      "there is work" should {
        val work = Work(WorkId("123"), "something")
        val workState = WorkState.empty.updated(WorkAccepted(work))

        "accept the register and send work ready" in new WorkManagerFixture(workState) with TestImageFixture {
          workManager ! messages.NewWork(operations.Resize(imageData, 50, 50))
          expectMsgPF(3.seconds) { case answers.NewWorkAck(_) => }

          workManager ! messages.RegisterWorker(workerId)
          expectMsg(answers.WorkIsReady)
        }
      }
    }

    "a registered worker requests work" when {
      "there is work" should {
        val workId = WorkId("456")
        val work = Work(workId, "something")
        val workState = WorkState.empty
          .updated(WorkAccepted(work))

        "reply with work" in new WorkManagerFixture(workState) {
          withRegisteredWorker(workManager) {
            underlyingActor.workers.get(workerId).get.status shouldBe WorkerIdle
            workManager ! messages.RequestWork(workerId)
            expectMsgPF(1.seconds) { case _: Work[_] => }

            underlyingActor.workers.get(workerId).get.status shouldBe a [WorkerBusy]
            underlyingActor.workState.isInProgress(workId) shouldBe true
            underlyingActor.workState.isPending(workId) shouldBe false
          }
        }
      }
    }

    "a worker reports WorkIsDone" when {
      "work was in progress" should {
        val workId = WorkId("123")
        val workState = WorkState.empty
          .updated(WorkAccepted(Work(workId, "something")))
          .updated(WorkStarted(workId))
        val workers = Map(
          workerId -> WorkerState(testActor, WorkerBusy(workId, Deadline.now + 10.seconds))
        )

        "ack, change worker to idle" in new WorkManagerFixture(workState, workers)
          with TestImageFixture {

          withRegisteredWorker(workManager) {
            underlyingActor.workers.get(workerId).get.status shouldBe a [WorkerBusy]

            workManager ! messages.WorkIsDone(
              workerId,
              workId,
              ProcessedImage(imageData, "image.jpg")
            )
            expectMsg(answers.WorkIsDoneAck(workId))

            underlyingActor.workers.get(workerId).get.status shouldBe WorkerIdle
          }
        }
      }
    }

    "a worker reports WorkFailed" when {
      "work was in progress" should {
        val workId = WorkId("123")
        val workState = WorkState.empty
          .updated(WorkAccepted(Work(workId, "something")))
          .updated(WorkStarted(workId))
        val workers = Map(
          workerId -> WorkerState(testActor, WorkerBusy(workId, Deadline.now + 10.seconds))
        )

        "update work state and retry" in new WorkManagerFixture(workState, workers) {
          withRegisteredWorker(workManager) {
            underlyingActor.workers.get(workerId).get.status shouldBe a [WorkerBusy]

            workManager ! messages.WorkFailed(workerId, workId)
            awaitCond { !underlyingActor.workState.isInProgress(workId) }

            val state = underlyingActor.workState
            state.isPending(workId) shouldBe true
            state.isDone(workId) shouldBe false
          }
        }
      }
    }

    "the queue has space" should {
      "accept work and ack" in new WorkManagerFixture with TestImageFixture {
        withRegisteredWorker(workManager) {

        }
      }
    }
  }
}
