package actors

import actors.systems.WorkerActorSystem
import akka.actor._
import akka.routing.BalancingPool
import akka.testkit._
import ammonite.ops._
import org.scalatest._
import services.ProcessedImage
import ImageProcessingWorker.messages
import ImageProcessingWorker.answers

import scala.concurrent.duration._

object ImageProcessingWorkerSpec extends WorkerActorSystem

class ImageProcessingWorkerSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {


  def this() = this(ImageProcessingWorkerSpec.system)

  val fileName = "black_mage_cat.jpg"
  val imageData = read.bytes(resource/fileName)

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  val workerPool: ActorRef = system.actorOf(
    BalancingPool(
      nrOfInstances = 3,
      supervisorStrategy = ImageProcessingWorker.supervisorStrategy
    ).props(ImageProcessingWorker.props)
  )

  "A worker actor" when {

    "receiving work" must {

      "ack back and respond with processed image" in {
        workerPool ! messages.Resize(imageData, 100, 100)
        expectMsg(3.seconds, answers.WorkAcknowledged)
        expectMsgPF(3.seconds) {
          case answers.WorkDone(_: ProcessedImage) => ()
        }
        expectNoMsg
      }
    }
  }
}
