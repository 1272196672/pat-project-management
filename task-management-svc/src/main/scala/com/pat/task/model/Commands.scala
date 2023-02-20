package com.pat.task.model

import akka.actor.typed.ActorRef

sealed trait Command extends CborSerializable

// 用户登录命令
final case class LoginCommand(staffId: String, staffState: Int, replyTo: ActorRef[Response]) extends Command

// 通用命令
final case class ShowProgressCommand(replyTo: ActorRef[Response]) extends Command with TestCommand with DevCommand
final case class UpdateProgressCommand(progress: Double, replyTo: ActorRef[Response]) extends Command with TestCommand with DevCommand

// 开发者命令
sealed trait DevCommand extends Command
final case class Send2TestCommand(testerId: String, reply: ActorRef[Response], replyToTest: ActorRef[TestCommand]) extends DevCommand

// 测试者命令
sealed trait TestCommand extends Command
final case class ReceiveFromDevCommand(testerId: String, replyToDev: ActorRef[Response]) extends TestCommand
