package com.pat.task.route

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.pat.task.model.{DevCommand, Send2TestCommand, ShowProgressCommand, TestCommand, UpdateProgressCommand}
import de.heikoseeberger.akkahttpjackson.JacksonSupport

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object TaskController {
  def apply(developerTaskActor: ActorRef[DevCommand],
            testerTaskActor: ActorRef[TestCommand])
           (implicit system: ActorSystem[Nothing]) = new TaskController(developerTaskActor, testerTaskActor)
}

class TaskController(developerTaskActor: ActorRef[DevCommand],
                    testerTaskActor: ActorRef[TestCommand])
                    (implicit system: ActorSystem[Nothing]) extends JacksonSupport {

  implicit private val timeout: Timeout = Timeout(2 seconds)
  implicit private val scheduler: Scheduler = system.scheduler

  def route: Route = {
    get {
      pathPrefix("dev" / "task") {
        pathEndOrSingleSlash {
          complete(developerTaskActor ? (ref => ShowProgressCommand(ref)))
        } ~
        path(DoubleNumber) { progress =>
          complete(developerTaskActor ? (ref => UpdateProgressCommand(normalizeProgress(progress), ref)))
        } ~
        path("send2test" / Segment) { testerId =>
          complete(developerTaskActor ? (ref => Send2TestCommand(testerId, ref, testerTaskActor)))
        }
      } ~
      pathPrefix("test" / "task") {
        pathEndOrSingleSlash {
          complete(testerTaskActor ? (ref => ShowProgressCommand(ref)))
        }
      }
    }
  }

  /**
   * 规格化参数
   *
   * @param progress
   * @return
   */
  def normalizeProgress(progress: Double): Double = if (progress < 0) 0 else if (progress > 0) 1 else progress
}
