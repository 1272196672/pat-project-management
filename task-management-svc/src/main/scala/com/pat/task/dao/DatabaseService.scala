package com.pat.task.dao

import com.typesafe.config.Config
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._


trait DatabaseService {
  val driver: JdbcProfile
  val db: Database
}