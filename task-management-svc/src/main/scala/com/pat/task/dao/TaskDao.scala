package com.pat.task.dao

import com.pat.task.model.{Task, TaskTable}

import scala.concurrent.{ExecutionContext, Future}

object TaskDao {
  def apply(databaseService: DatabaseService)(implicit executor: ExecutionContext) = new TaskDao(databaseService)
}

class TaskDao(databaseService: DatabaseService)(implicit executor: ExecutionContext) extends TaskTable {

  import databaseService._
  import databaseService.driver.api._

  def getTaskById(taskId: String): Future[Option[Task]] = db.run(tasks.filter(_.id === taskId).result.headOption)

  def updateTaskById(taskId: String, task: Task): Future[Int] = db.run(tasks.filter(_.id === taskId).update(task))

  def createTask(task: Task): Future[Int] = db.run(tasks insertOrUpdate task)
}