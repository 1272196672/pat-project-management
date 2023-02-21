package com.pat.task.model

final case class State(taskId: String, progress: Double, taskState: Int, doer: Int, handlerId: String) extends CborSerializable {
  def updateProgress(taskId: String, progress: Double, taskState: Int, doer: Int, handlerId: String): State = copy(taskId, progress, taskState, doer, handlerId)
  def completeTask: State = copy(taskId, 1, TaskState.COMPLETED, StaffState.DEVELOPER, handlerId)
  def deliverTask: State = copy(taskId, 1, TaskState.DELIVERED, StaffState.DEVELOPER, handlerId)
  def toSummary: Summary = Summary(taskId, progress, taskState, doer, handlerId)
}

object State {
  val empty: State = State("", progress = 0, taskState = 0, doer = 0, handlerId = "")
  def init(taskId: String, doer: Int, handlerId: String): State = State(taskId, 0, 0, doer, handlerId)
}

/**
 * taskState: 0为待办， 1为进行中，2为已完成, 3已发送
 */
object TaskState {
  final val UN_STARTED = 0
  final val PROCESSING = 1
  final val COMPLETED = 2
  final val DELIVERED = 3
}

/**
 * 0管理员， 1项目负责人，2产品经理，3开发人员，4测试人员
 */
object StaffState {
  final val ADMIN = 0
  final val PROJECT_LEADER = 1
  final val PRODUCT_MANAGER = 2
  final val DEVELOPER = 3
  final val TESTER = 4
}
