package com.rr.example

import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Greeter {
  final case class Greet(whom: String, replyTo: ActorRef[Greeted])
  final case class Greeted(whom: String)

  val initialBehavior: Behaviors.Immutable[Greet] = Behaviors.immutable[Greet] { (_, msg) ⇒
    println(s"Hello ${msg.whom}!")
    msg.replyTo ! Greeted(msg.whom)
    Behaviors.same
  }
}

object Main extends App {
  
  val main: Behavior[NotUsed] =
    Behaviors.setup { ctx ⇒
      var count = 2
      val screenName1 = "Screen Name 1"
      val screenName2 = "Screen Name 2"
      val chatRoom = ctx.spawn(ChatRoomActor.initialBehavior, "chatroom")
      val client1: ActorRef[ChatSessionActor.ChatSessionEvent] = ctx.spawn(ExampleChatClientActor.initialBehaviour(screenName1), "client1")
      ctx.watch(client1)
      val client2: ActorRef[ChatSessionActor.ChatSessionEvent] = ctx.spawn(ExampleChatClientActor.initialBehaviour(screenName2), "client2")
      ctx.watch(client2)

      chatRoom ! ChatRoomActor.GetChatSession(screenName1, client1)
      chatRoom ! ChatRoomActor.GetChatSession(screenName2, client2)

      Behaviors.onSignal {
        case (_, Terminated(ref)) ⇒
          count = count - 1
          println(s"Terminated $ref count is $count")
          if (count == 0) Behaviors.stopped else Behaviors.same
      }

    }

  val system = ActorSystem(main, "ChatRoomDemo")
  Await.result(system.whenTerminated, 3.seconds)


}

