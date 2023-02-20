package com.pat.task.service

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, MailboxSelector, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import com.pat.task.WebServer
import com.pat.task.model.{Command, LoginCommand, StaffInfo, StaffState, SuccessResponse}
import com.pat.task.route.{DevController, LoginController, TesterController}

object LoginActor {
  def apply(): Behavior[LoginCommand] = {
    Behaviors.setup { context =>
      implicit val system: ActorSystem[Nothing] = context.system
      Behaviors.receiveMessage {
        case LoginCommand(staffId, staffState, replyTo) =>
          replyTo ! SuccessResponse(StaffInfo(staffId, staffState))
          val host = system.settings.config.getString("akka.http.host")
          staffState match {
            case StaffState.DEVELOPER =>
              val developerTaskActor = context.spawn(
                Behaviors.supervise(DeveloperTaskActor(staffId)).onFailure(SupervisorStrategy.restart),
                s"DeveloperTaskActor-$staffId"
              )
              val port = system.settings.config.getInt("akka.http.staff.developer-port")
              WebServer.start(host, port, DevController(developerTaskActor).route)

            case StaffState.TESTER =>
              val testerTaskActor = context.spawn(
                Behaviors.supervise(TesterTaskActor(staffId)).onFailure(SupervisorStrategy.restart),
                s"TesterTaskActor-$staffId"
              )
              val port = system.settings.config.getInt("akka.http.staff.tester-port")
              WebServer.start(host, port, TesterController(testerTaskActor).route)
          }
          Behaviors.same
      }
    }
  }
}
