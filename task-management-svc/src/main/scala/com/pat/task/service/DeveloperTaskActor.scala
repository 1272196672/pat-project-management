package com.pat.task.service

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Scheduler, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import akka.util.Timeout
import com.pat.task.model._

import scala.concurrent.duration.DurationInt
import cn.hutool.core.util.IdUtil

import scala.language.postfixOps

object DeveloperTaskActor {
  val taskId: String = IdUtil.simpleUUID()
  def apply(): Behavior[DevCommand] = {
    Behaviors.setup[DevCommand] { context =>
      val scheduler: Scheduler = context.system.scheduler
      EventSourcedBehavior[DevCommand, Event, State](
        PersistenceId("DeveloperTaskActor", taskId),
        State.dev,
        (state, command) =>
          if (state.taskState == TaskState.DELIVERED) haveNoRightToModify(state, command)
          else openTask(taskId, state, command, scheduler),
        (state, event) => handleEvent(state, event)
      )
        .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
        .onPersistFailure(SupervisorStrategy.restartWithBackoff(200 millis, 5 seconds, 0.1))
    }
  }

  implicit val timeout: Timeout = Timeout(2 seconds)

  // 接受命令，操作任务
  private def openTask(taskId: String, state: State, command: DevCommand, scheduler: Scheduler): Effect[Event, State] = {

    implicit val scheduler1: Scheduler = scheduler

    command match {
      case UpdateProgressCommand(progress, replyTo) =>
        Effect
          .persist(
            if (progress == 1)
              TaskCompletedEvent(taskId, state.doer)
            else
              ProgressUpdatedEvent(taskId, progress, TaskState.PROCESSING, state.doer)
          )
          .thenRun(updateProgress => replyTo ! SuccessResponse(updateProgress.toSummary))

      case Send2TestCommand(testerId, replyTo, replyToTest) =>
        if (state.taskState == TaskState.COMPLETED) {
          Effect
            .persist(Sent2TestEvent(taskId, testerId))
            .thenRun(_ => replyToTest ! ReceiveFromDevCommand(testerId, replyTo))
          /*
          .thenRun(_ => {
            val future = replyToTest ? (ref => ReceiveFromDevCommand(ref))
            val response = Await.result(future, 2 seconds)
            println(1)
            replyTo ! response
          })

           */
        } else {
          replyTo ! ErrorResponse("TASK NOT COMPLETED!")
          Effect.none
        }

      case ShowProgressCommand(replyTo) =>
        replyTo ! SuccessResponse(state.toSummary)
        Effect.none

      case _ => Effect.none
    }
  }

  // 没有权限操作
  private def haveNoRightToModify(state: State, command: DevCommand): Effect[Event, State] = {
    command match {
      case UpdateProgressCommand(_, replyTo) =>
        replyTo ! ErrorResponse("NO RIGHT!")
        Effect.none

      case ShowProgressCommand(replyTo) =>
        replyTo ! SuccessResponse(state.toSummary)
        Effect.none

      case Send2TestCommand(_, replyTo, _) =>
        replyTo ! ErrorResponse("NO RIGHT!")
        Effect.none

      case _ =>
        Effect.none
    }
  }

  // 处理事件
  private def handleEvent(state: State, event: Event): State = {
    event match {
      case ProgressUpdatedEvent(taskId, progress, taskState, doer) => state.updateProgress(progress, taskState, state.handlerId)

      case TaskCompletedEvent(taskId, doer) => state.completeTask

      case Sent2TestEvent(taskId, testerId) => state.deliverTask
    }
  }
}
