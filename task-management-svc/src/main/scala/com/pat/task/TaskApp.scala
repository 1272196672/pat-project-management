package com.pat.task

import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory

object TaskApp extends App {
  ActorSystem[Nothing](Guardian(), "task-management-svc", ConfigFactory.load("application-node4.conf"))
}

object TaskApp2 extends App {
  ActorSystem[Nothing](Guardian(), "task-management-svc", ConfigFactory.load("application-node5.conf"))
}
