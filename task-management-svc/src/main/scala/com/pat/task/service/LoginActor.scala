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
              val port = system.settings.config.getInt("akka.http.staff.developer-port")
              try {
                developerTaskActor = context.spawn(
                  Behaviors.supervise(DeveloperTaskActor(staffId)(databaseService)).onFailure(SupervisorStrategy.stop),
                  s"DeveloperTaskActor-$staffId"
                )
              } catch {
                case e: Exception =>
                  system.log.error(s"${e}")
                  WebServer.stop
                  WebServer.start(host, port, DevController(developerTaskActor)(databaseService).route)
                  replyTo ! SuccessResponse(StaffInfo(staffId, staffState))
                  Behaviors.same
              }
              WebServer.start(host, port, DevController(developerTaskActor)(databaseService).route)

            case StaffState.TESTER =>
              val port = system.settings.config.getInt("akka.http.staff.tester-port")
              try {
                testerTaskActor = context.spawn(
                  Behaviors.supervise(TesterTaskActor(staffId)(databaseService)).onFailure(SupervisorStrategy.stop),
                  s"TesterTaskActor-$staffId"
                )
              } catch {
                case e: Exception =>
                  system.log.error(s"${e}")
                  WebServer.stop
                  WebServer.start(host, port, TesterController(testerTaskActor).route)
                  replyTo ! SuccessResponse(StaffInfo(staffId, staffState))
                  Behaviors.same
              }
              WebServer.start(host, port, TesterController(testerTaskActor).route)
          }
          replyTo ! SuccessResponse(StaffInfo(staffId, staffState))
          Behaviors.same
      }
    }
  }
}
