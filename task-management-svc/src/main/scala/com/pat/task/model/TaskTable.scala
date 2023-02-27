package com.pat.task.model

import slick.jdbc.MySQLProfile.api._

trait TaskTable {

  val tasks = TableQuery[TaskInfo]

  val tableName: String = "t_task"

  class TaskInfo(tag: Tag) extends Table[Task](tag, tableName) {
    def id = column[String]("id", O.PrimaryKey, O.SqlType("varchar"))

    def title = column[String]("title", O.SqlType("varchar"))

    def description = column[String]("description", O.SqlType("varchar"))

    def progress = column[Double]("progress", O.SqlType("double"))

    def taskState = column[Int]("task_state", O.SqlType("int"))

    def doer = column[Int]("doer", O.SqlType("int"))

    def handlerId = column[String]("handler_id", O.SqlType("varchar"))

    override def * = (id, title, description, progress, taskState, doer, handlerId) <> (Task(_), Task.unapply)
  }
}
