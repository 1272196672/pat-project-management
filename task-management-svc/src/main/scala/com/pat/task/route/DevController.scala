package com.pat.task.route

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.pat.task.model.{DevCommand, GetTaskCommand, Send2TestCommand, ShowProgressCommand, TestCommand, UpdateProgressCommand}
import com.pat.task.service.TesterTaskActor
import com.pat.task.util.NormalizationUtils
import de.heikoseeberger.akkahttpjackson.JacksonSupport

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object DevController {
  def apply(developerTaskActor: ActorRef[DevCommand])(implicit system: ActorSystem[Nothing]) = new DevController(developerTaskActor)
}

class DevController(developerTaskActor: ActorRef[DevCommand])(implicit system: ActorSystem[Nothing]) extends JacksonSupport {

  implicit private val timeout: Timeout = Timeout(2 seconds)
  implicit private val scheduler: Scheduler = system.scheduler

  def route: Route = {
    get {
      pathPrefix("dev" / "task" / Segment) { taskId =>
        pathEndOrSingleSlash {
          complete(developerTaskActor ? (ref => ShowProgressCommand(taskId, ref)))
        } ~
        path("getTask") {
          complete(developerTaskActor ? (ref => GetTaskCommand(taskId, ref)))
        } ~
        path(DoubleNumber) { progress =>
          complete(developerTaskActor ? (ref => UpdateProgressCommand(taskId, NormalizationUtils.normalizeProgress(progress), ref)))
        } ~
        path("send2test" / Segment) { testerId =>
          val testerTaskActor = system.systemActorOf(TesterTaskActor(testerId), s"TesterTaskActor-$testerId")
          complete(developerTaskActor ? (ref => Send2TestCommand(taskId, testerId, ref, testerTaskActor)))
        }
      }
    }
  }
}
