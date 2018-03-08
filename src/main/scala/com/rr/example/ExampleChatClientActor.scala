package com.rr.example

import akka.actor.typed.scaladsl.Behaviors

// A reference to this actor (ActorRef[ChatSessionEvent]) is provided when a session is created
// This version responds to the SessionGranted event by posting a message to the chat
// When notified about messages that have been posted (by any client) this version prints the message
// This client is stopped once it has posted its Hello message

object ExampleChatClientActor {

  def initialBehaviour(screenNameForClient: String): Behaviors.Immutable[ChatSessionActor.ChatSessionEvent] =
    Behaviors.immutable[ChatSessionActor.ChatSessionEvent] { (x, msg) ⇒
      msg match {
        case ChatSessionActor.SessionGranted(session) ⇒
          //println(s"posting message from ${x.self}")
          session ! ChatSessionActor.PostMessage(s"Hello World from ${x.self}")
          Behaviors.same
        case ChatSessionActor.MessagePosted(screenName, message) ⇒
          println(s"message posted by '$screenName': $message seen by ${x.self}")
          if (screenName == screenNameForClient) Behaviors.stopped else Behaviors.same
      }
    }

}
