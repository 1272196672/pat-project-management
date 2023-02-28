package com.pat.task.service

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import cn.hutool.core.util.IdUtil
import com.pat.task.dao.DatabaseService
import com.pat.task.model._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


object TesterTaskActor {
  def apply(staffId: String)(databaseService: DatabaseService): Behavior[TestCommand] = {
    Behaviors.setup[TestCommand] { context =>
      EventSourcedBehavior[TestCommand, Event, State](
        PersistenceId("TesterTaskActor", staffId),
        State.empty,
        (state, command) => openTask(staffId, state, command),
        (state, event) => handleEvent(state, event)
      )
        .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
        .onPersistFailure(SupervisorStrategy.restartWithBackoff(200 millis, 5 seconds, 0.1))
    }
  }

  def openTask(staffId: String, state: State, command: TestCommand): Effect[Event, State] = {
    command match {
      case ReceiveFromDevCommand(taskId, testerId, replyTo) =>
        replyTo ! SuccessResponse("GOT IT")
        Effect
          .persist(ReceiveFromDevEvent(staffId, taskId, testerId))
          .thenStop()
      case ShowProgressCommand(taskId, replyTo) =>
        replyTo ! SuccessResponse(state.toSummary)
        Effect.none
    }
  }

  def handleEvent(state: State, event: Event): State = {
    event match {
      case ReceiveFromDevEvent(staffId, taskId, testerId) => state.updateProgress(taskId, 0, TaskState.PROCESSING, StaffState.TESTER, staffId)
    }
  }
}
