package com.pat.task.model

sealed trait Event extends CborSerializable

sealed trait TaskEvent extends Event {
  def staffId: String
}

// 通用Event
final case class ProgressUpdatedEvent(staffId: String, taskId: String, progress: Double, taskState: Int, doer: Int) extends TaskEvent
final case class TaskCompletedEvent(staffId: String, taskId: String, doer: Int) extends TaskEvent

// 开发者Event
sealed trait DevEvent extends TaskEvent
final case class Sent2TestEvent(staffId: String, taskId: String, testerId: String) extends DevEvent
final case class TaskGottenEvent(staffId: String, taskId: String) extends DevEvent

// 测试人员Event
sealed trait TestEvent extends TaskEvent
final case class ReceiveFromDevEvent(staffId: String, taskId: String, testerId: String) extends TestEvent

