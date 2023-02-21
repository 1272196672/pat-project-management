package com.pat.task.route

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.pat.task.model.{DevCommand, Send2TestCommand, ShowProgressCommand, TestCommand, UpdateProgressCommand}
import com.pat.task.service.TesterTaskActor
import de.heikoseeberger.akkahttpjackson.JacksonSupport

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object TesterController {
  def apply(testerTaskActor: ActorRef[TestCommand])(implicit system: ActorSystem[Nothing]) = new TesterController(testerTaskActor)
}

class TesterController(testerTaskActor: ActorRef[TestCommand])(implicit system: ActorSystem[Nothing]) extends JacksonSupport {

  implicit private val timeout: Timeout = Timeout(2 seconds)
  implicit private val scheduler: Scheduler = system.scheduler

  def route: Route = {
    get {
      pathPrefix("test" / "task" / Segment) { taskId =>
        pathEndOrSingleSlash {
          complete(testerTaskActor ? (ref => ShowProgressCommand(taskId, ref)))
        }
      }
    }
  }
}
