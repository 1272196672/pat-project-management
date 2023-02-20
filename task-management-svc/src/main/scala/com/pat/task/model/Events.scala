package com.pat.task.model

sealed trait Event extends CborSerializable

sealed trait TaskEvent extends Event {
  def taskId: String
}

// 通用Event
final case class ProgressUpdatedEvent(taskId: String, progress: Double, taskState: Int, doer: Int) extends TaskEvent
final case class TaskCompletedEvent(taskId: String, doer: Int) extends TaskEvent

// 开发者Event
sealed trait DevEvent extends TaskEvent
final case class Sent2TestEvent(taskId: String, testerId: String) extends DevEvent

// 测试人员Event
sealed trait TestEvent extends TaskEvent
final case class ReceiveFromDevEvent(taskId: String, testerId: String) extends TestEvent

