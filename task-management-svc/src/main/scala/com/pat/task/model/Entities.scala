package com.pat.task.model

import akka.http.scaladsl.model.DateTime

trait Entity extends CborSerializable

final case class Summary(progress: Double, taskState: Int, doer: Int, handlerId: String) extends CborSerializable

final case class TaskEntity(id: Int,
                            projectId: String,
                            creatorId: String,
                            doerId: String,
                            description: String,
                            isComplete: Boolean,
                            createTime: DateTime,
                            startTime: DateTime,
                            modifyTime: DateTime
                           ) extends Entity