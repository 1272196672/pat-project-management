package com.pat.task.model

sealed trait Response extends CborSerializable {
  val code: Int
}

final case class SuccessResponse(val data: AnyRef, val code: Int = 20000, msg: String = "Success!") extends Response

final case class ErrorResponse(msg: String, val code: Int = 50000, data: Option[String] = None) extends Response