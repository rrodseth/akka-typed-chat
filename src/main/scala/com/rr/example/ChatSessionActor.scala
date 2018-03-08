package com.rr.example

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

/*
  Links a room with a client
  There is one session per client, and the session has references to the room and client

 */
object ChatSessionActor {

  sealed trait ChatSessionEvent
  final case class SessionGranted(handle: ActorRef[PostMessage]) extends ChatSessionEvent
  final case class SessionDenied(reason: String) extends ChatSessionEvent
  final case class MessagePosted(screenName: String, message: String) extends ChatSessionEvent

  trait ChatSessionCommand
  final case class PostMessage(message: String) extends ChatSessionCommand
  /*private */final case class NotifyClient(message: MessagePosted) extends ChatSessionCommand


  /*private */
  def initialBehavior(room: ActorRef[ChatRoomActor.PublishChatMessage],
                      screenName: String,
                      client: ActorRef[ChatSessionEvent]): Behavior[ChatSessionCommand] =
    Behaviors.immutable { (ctx, msg) ⇒
      msg match {
        case PostMessage(message) ⇒
          // from client, publish to others via the room
          room ! ChatRoomActor.PublishChatMessage(screenName, message)
          Behaviors.same
        case NotifyClient(message) ⇒
          // published from the room
          client ! message
          Behaviors.same
      }
    }

}
