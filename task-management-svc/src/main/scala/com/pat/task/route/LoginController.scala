package com.pat.task.route

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.server.Directives._
import com.pat.task.model.LoginCommand
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object LoginController {
  def apply(loginActor: ActorRef[LoginCommand])(implicit system: ActorSystem[Nothing]) = new LoginController(loginActor)

}

class LoginController(loginActor: ActorRef[LoginCommand])(implicit system: ActorSystem[Nothing]) extends JacksonSupport {

  implicit private val timeout: Timeout = Timeout(4 seconds)
  implicit private val scheduler: Scheduler = system.scheduler

  def route: Route = {
    post {
      path("login") {
        formFields("staffId", "staffState".as[Int]) { (staffId, staffState) =>
          complete(loginActor ? (ref => LoginCommand(staffId, staffState, ref)))
        }
      }
    }
  }
}
