package com.pat.task.model

import akka.http.scaladsl.model.DateTime

trait Entity extends CborSerializable

final case class Summary(taskId: String, progress: Double, taskState: Int, doer: Int, handlerId: String) extends CborSerializable

final case class StaffInfo(staffId: String, staffState: Int) extends CborSerializable

final case class Task(id: String,
                      progress: Double,
                      taskState: Int,
                      doer: Int,
                      handlerId: String) extends Entity {
  def this(tuple: (String, Double, Int, Int, String)) = this(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5)
}

object Task {
  def apply(tuple: (String, Double, Int, Int, String)) = new Task(tuple)
}