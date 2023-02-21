package com.pat.task.service

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Scheduler, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import akka.util.Timeout
import com.pat.task.model._

import scala.concurrent.duration.DurationInt
import cn.hutool.core.util.IdUtil
import com.pat.task.dao.{DatabaseService, TaskDao}

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.language.postfixOps

object DeveloperTaskActor {
  def apply(staffId: String)(databaseService: DatabaseService): Behavior[DevCommand] = {

    Behaviors.setup[DevCommand] { context =>
      val scheduler: Scheduler = context.system.scheduler
      implicit val executionContext: ExecutionContextExecutor = context.executionContext
      EventSourcedBehavior[DevCommand, Event, State](
        PersistenceId("DeveloperTaskActor", staffId),
        State.empty,
        (state, command) =>
          if (state.taskState == TaskState.DELIVERED) haveNoRightToModify(state, command)
          else openTask(staffId, state, command, scheduler, TaskDao(databaseService)),
        (state, event) => handleEvent(state, event)
      )
        .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
        .onPersistFailure(SupervisorStrategy.restartWithBackoff(200 millis, 5 seconds, 0.1))
    }
  }

  implicit val timeout: Timeout = Timeout(2 seconds)

  // 接受命令，操作任务
  private def openTask(staffId: String, state: State, command: DevCommand, scheduler: Scheduler, taskDao: TaskDao): Effect[Event, State] = {

    implicit val scheduler1: Scheduler = scheduler

    command match {
      case GetTaskCommand(taskId, replyTo) =>
        Effect
          .persist(TaskGottenEvent(staffId, taskId))
          .thenRun { showTask =>
            replyTo ! SuccessResponse(showTask.toSummary)
            taskDao.createTask(showTask.toTask)
          }

      case UpdateProgressCommand(taskId, progress, replyTo) =>
        Effect
          .persist(
            if (progress == 1)
              TaskCompletedEvent(staffId, taskId, state.doer)
            else
              ProgressUpdatedEvent(staffId, taskId, progress, TaskState.PROCESSING, StaffState.DEVELOPER)
          )
          .thenRun { showTask =>
            replyTo ! SuccessResponse(showTask.toSummary)
            taskDao.updateTaskById(taskId, showTask.toTask)
          }

      case Send2TestCommand(taskId, testerId, replyTo, replyToTest) =>
        if (state.taskState == TaskState.COMPLETED) {
          Effect
            .persist(Sent2TestEvent(staffId, taskId, testerId))
            .thenRun{ showTask =>
              replyToTest ! ReceiveFromDevCommand(taskId, testerId, replyTo)
              taskDao.updateTaskById(taskId, showTask.toTask)
            }
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

      case ShowProgressCommand(taskId, replyTo) =>
        replyTo ! SuccessResponse(state.toSummary)
        Effect.none

      case _ => Effect.none
    }
  }

  // 没有权限操作
  private def haveNoRightToModify(state: State, command: DevCommand): Effect[Event, State] = {
    command match {
      case UpdateProgressCommand(taskId, _, replyTo) =>
        replyTo ! ErrorResponse("NO RIGHT!")
        Effect.none

      case ShowProgressCommand(taskId, replyTo) =>
        replyTo ! SuccessResponse(state.toSummary)
        Effect.none

      case Send2TestCommand(taskId, _, replyTo, _) =>
        replyTo ! ErrorResponse("NO RIGHT!")
        Effect.none

      case _ =>
        Effect.none
    }
  }

  // 处理事件
  private def handleEvent(state: State, event: Event): State = {
    event match {
      case TaskGottenEvent(staffId, taskId) =>  state.updateProgress(taskId, 0, TaskState.UN_STARTED, StaffState.DEVELOPER, staffId)

      case ProgressUpdatedEvent(staffId, taskId, progress, taskState, doer) => state.updateProgress(taskId, progress, taskState, doer, staffId)

      case TaskCompletedEvent(staffId, taskId, doer) => state.completeTask

      case Sent2TestEvent(staffId, taskId, testerId) => state.deliverTask
    }
  }
}
