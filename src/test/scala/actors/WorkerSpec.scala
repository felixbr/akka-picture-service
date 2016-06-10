package actors

import actors.WorkManager.messages.{RegisterWorker, WorkIsDone}
import akka.actor._
import akka.testkit._
import ammonite.ops._
import models.core._
import models.operations
import org.scalatest._
import services.ProcessedImage

import scala.concurrent.duration._

class WorkerSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem())

  val fileName = "black_mage_cat_100.jpg"
  val imageData = read.bytes(resource/fileName)

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  ignoreMsg { case _: RegisterWorker  => true }
  val worker: ActorRef = system.actorOf(Worker.props(self))

  val workId = WorkId("123")

  "A worker actor" when {
    "receiving work" must {
      "respond with processed image" in {
        worker ! Work(workId, operations.Resize(imageData, 100, 100))
        expectMsgPF(3.seconds) {
          case WorkIsDone(_, `workId`, _: ProcessedImage) =>
        }
        expectNoMsg
      }
    }
  }
}
