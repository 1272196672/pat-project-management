package com.pat.task.route

import akka.actor.typed.{ActorRef, ActorSystem}
import com.pat.task.model.LoginCommand
import de.heikoseeberger.akkahttpjackson.JacksonSupport

object LoginController {
  def apply(LoginActor: ActorRef[LoginCommand])(implicit system: ActorSystem[Nothing]) = new LoginController(LoginActor)

}

class LoginController(LoginActor: ActorRef[LoginCommand])(implicit system: ActorSystem[Nothing]) extends JacksonSupport {

}
