package com.pat.task.model

final case class State(progress: Double, taskState: Int, doer: Int, handlerId: String) extends CborSerializable {
  def updateProgress(progress: Double, taskState: Int, handlerId: String): State = copy(progress, taskState, handlerId = handlerId)
  def completeTask: State = copy(1, TaskState.COMPLETED, StaffState.DEVELOPER, handlerId)
  def deliverTask: State = copy(1, TaskState.DELIVERED, StaffState.DEVELOPER, handlerId)
  def toSummary: Summary = Summary(progress, taskState, doer, handlerId)
}

object State {
  val empty: State = State(progress = 0, taskState = 0, doer = 0, handlerId = "")
  val dev: State = State(progress = 0, taskState = 0, doer = StaffState.DEVELOPER, handlerId = "DeveloperA")
  val test: State = State(progress = 0, taskState = 0, doer = StaffState.TESTER, handlerId = "TesterA")
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
