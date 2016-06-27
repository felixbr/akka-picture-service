package admin

import actors.PushMessageActor
import admin.clientServerProtocol.toClient._
import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws._
import akka.stream._
import akka.stream.scaladsl._
import upickle.default._

object live {

  def wsHandlerFlow(implicit system: ActorSystem): Flow[Message, Message, Any] = {
    val g = GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val pushSource = b.add(Source.actorPublisher[Any](PushMessageActor.props))
      val transformToClientMessage = b.add(Flow[Any].collect[ToClientMessage](toClientMessage))

      val pushToClientMessageFlow = b.add(Flow[ToClientMessage].map[Message](pushToClientMessage))

      val messageFlow = b.add(Flow[Message].collect(processClientMessage))

      val mergeFlow = b.add(Merge[Message](2))

                                                            messageFlow ~> mergeFlow
      pushSource ~> transformToClientMessage ~> pushToClientMessageFlow ~> mergeFlow

      FlowShape(messageFlow.in, mergeFlow.out)
    }

    Flow.fromGraph(g)
  }

  private def pushToClientMessage(message: ToClientMessage): TextMessage.Strict = {
    TextMessage.Strict(write(message))
  }

  private val TaggedJson = "(\\w+)\\|(.+)".r
  private val processClientMessage: PartialFunction[Message, Message] = {
    case _ => TextMessage.Strict("ack")
  }

}
