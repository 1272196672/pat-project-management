package com.pat.task

import akka.Done
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, MailboxSelector, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.pat.task.dao.{DatabaseService, MysqlService}
import com.pat.task.model.{DevCommand, LoginCommand, TestCommand}
import com.pat.task.route.LoginController
import com.pat.task.service.{DeveloperTaskActor, LoginActor, TesterTaskActor}

import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps

object Guardian {
  def apply(): Behavior[Nothing] = {
    Behaviors.setup[Nothing] { context =>
      implicit val system: ActorSystem[Nothing] = context.system
      implicit val executionContext: ExecutionContextExecutor  = system.executionContext

      Cluster(system)
      context.spawn(ClusterListener.listen(), "clusterListener")

      val config = context.system.settings.config
      val httpHost = config.getString("akka.http.host")
      val httpPort = config.getInt("akka.http.port")
      /*
      val developerTaskActor: ActorRef[DevCommand] = context.spawn(Behaviors.supervise(DeveloperTaskActor()).onFailure(SupervisorStrategy.restart), "developerTaskActor", MailboxSelector.defaultMailbox())
      val testerTaskActor: ActorRef[TestCommand] = context.spawn(Behaviors.supervise(TesterTaskActor()).onFailure(SupervisorStrategy.restart), "testerTaskActor", MailboxSelector.defaultMailbox())
       */
      val databaseService: DatabaseService = new MysqlService(config)
      val loginActor: ActorRef[LoginCommand] = context.spawn(Behaviors.supervise(LoginActor(databaseService)).onFailure(SupervisorStrategy.restart), "testerTaskActor", MailboxSelector.defaultMailbox())
      /*
      WebServer.start(httpHost, httpPort, TaskController(developerTaskActor, testerTaskActor).route)
       */
      val routes = LoginController(loginActor).route
      WebServer.start(httpHost, httpPort, routes)

      AkkaManagement(context.system).start
      ClusterBootstrap.get(system).start()

      Behaviors.empty
    }
  }
}
