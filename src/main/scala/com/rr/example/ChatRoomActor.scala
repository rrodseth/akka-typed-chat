package com.rr.example

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

/*
  Maintains a list of session child actors
  Sessions are created when responding to GetChatSession messages
  Responds to PublishChatMessage by notifying each session/client
 */

object ChatRoomActor {

  sealed trait ChatRoomCommand
  final case class GetChatSession(screenName: String, replyTo: ActorRef[ChatSessionActor.ChatSessionEvent])
    extends ChatRoomCommand

  /*private */final case class PublishChatMessage(screenName: String, message: String)
    extends ChatRoomCommand


  val initialBehavior: Behavior[ChatRoomCommand] =
    chatRoomBehavior(List.empty)

  private def chatRoomBehavior(sessions: List[ActorRef[ChatSessionActor.ChatSessionCommand]]): Behavior[ChatRoomCommand] =
    Behaviors.immutable[ChatRoomCommand] { (ctx, msg) ⇒
      msg match {

        case GetChatSession(screenName, client) ⇒
          // create a child actor for further interaction with the client
          val ses = ctx.spawn(
            ChatSessionActor.initialBehavior(ctx.self, screenName, client),
            name = URLEncoder.encode(screenName, StandardCharsets.UTF_8.name))
          //println(s"granting session for $screenName")
          client ! ChatSessionActor.SessionGranted(ses)
          // Return this behavior with updated state (adding the new session to the list)
          chatRoomBehavior(ses :: sessions)

        case PublishChatMessage(screenName, message) ⇒
          val notification = ChatSessionActor.NotifyClient(ChatSessionActor.MessagePosted(screenName, message))
          sessions foreach (_ ! notification)
          Behaviors.same
      }
    }


}
