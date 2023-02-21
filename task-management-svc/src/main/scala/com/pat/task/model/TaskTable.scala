package com.pat.task.model

import slick.jdbc.MySQLProfile.api._

trait TaskTable {

  val tasks = TableQuery[TaskInfo]

  val tableName: String = "task"

  class TaskInfo(tag: Tag) extends Table[Task](tag, tableName) {
    def id = column[String]("id", O.PrimaryKey, O.SqlType("varchar"))

    def progress = column[Double]("progress", O.SqlType("double"))

    def taskState = column[Int]("taskState", O.SqlType("int"))

    def doer = column[Int]("doer", O.SqlType("int"))

    def handlerId = column[String]("handlerId", O.SqlType("varchar"))

    override def * = (id, progress, taskState, doer, handlerId) <> (Task(_), Task.unapply)
  }

}
