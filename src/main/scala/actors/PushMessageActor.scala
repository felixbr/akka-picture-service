package actors

import akka.actor.{Actor, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.cluster.pubsub._
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request

import scala.annotation.tailrec

object PushMessageActor {
  def props = Props(new PushMessageActor)
}

class PushMessageActor extends ActorPublisher[Any] {

  val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe(WorkManagerMonitor.topic, self)

  var buf = Vector.empty[Any]

  def receive: Receive = {
    case Request(_) =>
      deliverBuf()

    case m =>
      if (buf.isEmpty && totalDemand > 0) {
        onNext(m)
      } else {
        buf :+= m
        deliverBuf()
      }
  }

  @tailrec private def deliverBuf(): Unit = {
    if (totalDemand > 0) {
      if (totalDemand < Int.MaxValue) {
        val (use, keep) = buf.splitAt(totalDemand.toInt)
        buf = keep
        use.foreach(onNext)

      } else {
        val (use, keep) = buf.splitAt(Int.MaxValue)
        buf = keep
        use.foreach(onNext)
        deliverBuf()
      }
    }
  }
}

trait ClusterMonitorSubscriber { self: Actor =>
  val cluster = Cluster(context.system)

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(this.self, initialStateMode = InitialStateAsSnapshot,
      classOf[MemberEvent], classOf[UnreachableMember])
    //#subscribe
  }
  override def postStop(): Unit = cluster.unsubscribe(this.self)
}
