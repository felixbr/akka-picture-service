package actors

import akka.actor.{Actor, ActorRef, Props}
import ammonite.ops._
import api.Server._
import models.operations

import scala.concurrent.duration._
import scala.util.Random

object WorkSpammerActor {
  def props(workReceiver: ActorRef) = Props(new WorkSpammerActor(workReceiver))
}

class WorkSpammerActor(workReceiver: ActorRef) extends Actor {
  val imageData = read.bytes(resource/"black_mage_cat_100.jpg")

  def randomSize() = Random.nextInt(2000) + 20

  system.scheduler.schedule(
    initialDelay = 1.seconds,
    interval = 2.seconds,
    receiver = self,
    message = "tick"
  )

  def receive: Receive = {
    case "tick" =>
      workReceiver ! WorkManager.messages.NewWork(
        operations.Resize(imageData, randomSize(), randomSize())
      )
  }
}
