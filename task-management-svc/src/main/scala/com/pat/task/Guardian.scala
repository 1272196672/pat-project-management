package com.pat.task

import akka.Done
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, MailboxSelector, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.pat.task.model.{DevCommand, TestCommand}
import com.pat.task.route.TaskController
import com.pat.task.service.{DeveloperTaskActor, TesterTaskActor}

import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps

object Guardian {
  def apply(): Behavior[Nothing] = {
    Behaviors.setup[Nothing] { context =>
      implicit val system: ActorSystem[Nothing] = context.system
      implicit val executionContext: ExecutionContextExecutor  = system.executionContext

      Cluster(system)
      context.spawn(ClusterListener.listen(), "clusterListener")

      val httpHost = context.system.settings.config.getString("akka.http.host")
      val httpPort = context.system.settings.config.getInt("akka.http.port")
      val developerTaskActor: ActorRef[DevCommand] = context.spawn(Behaviors.supervise(DeveloperTaskActor()).onFailure(SupervisorStrategy.restart), "developerTaskActor", MailboxSelector.defaultMailbox())
      val testerTaskActor: ActorRef[TestCommand] = context.spawn(Behaviors.supervise(TesterTaskActor()).onFailure(SupervisorStrategy.restart), "testerTaskActor", MailboxSelector.defaultMailbox())
      WebServer.start(httpHost, httpPort, TaskController(developerTaskActor, testerTaskActor).route)

      AkkaManagement(context.system).start
      ClusterBootstrap.get(system).start()

      Behaviors.empty
    }
  }
}
