package com.pat.task.service

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, MailboxSelector, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import com.pat.task.WebServer
import com.pat.task.dao.DatabaseService
import com.pat.task.model.{Command, DevCommand, LoginCommand, StaffInfo, StaffState, SuccessResponse, TestCommand}
import com.pat.task.route.{DevController, LoginController, TesterController}

object LoginActor {
  var testerTaskActor: ActorRef[TestCommand] = null
  var developerTaskActor: ActorRef[DevCommand] = null

  def apply(databaseService: DatabaseService): Behavior[LoginCommand] = {
    Behaviors.setup { context =>
      implicit val system: ActorSystem[Nothing] = context.system
      Behaviors.receiveMessage {
        case LoginCommand(staffId, staffState, replyTo) =>
          val host = system.settings.config.getString("akka.http.host")
          staffState match {
            case StaffState.DEVELOPER =>
              developerTaskActor = context.spawn(
                  Behaviors.supervise(DeveloperTaskActor(staffId)(databaseService)).onFailure(SupervisorStrategy.restart),
              s"DeveloperTaskActor-$staffId"
              )
              val port = system.settings.config.getInt("akka.http.staff.developer-port")
              WebServer.start(host, port, DevController(developerTaskActor)(databaseService).route)

            case StaffState.TESTER =>
              testerTaskActor = context.spawn(
                  Behaviors.supervise(TesterTaskActor(staffId)(databaseService)).onFailure(SupervisorStrategy.restart),
              s"TesterTaskActor-$staffId"
              )
              val port = system.settings.config.getInt("akka.http.staff.tester-port")
              WebServer.start(host, port, TesterController(testerTaskActor).route)
          }
          replyTo ! SuccessResponse(StaffInfo(staffId, staffState))
          Behaviors.same
      }
    }
  }
}
