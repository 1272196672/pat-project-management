package com.pat.task.dao

import com.typesafe.config.Config
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile

class MysqlService(config: Config) extends DatabaseService {
  override val driver = MySQLProfile
  override val db = Database.forConfig("slick.db", config)
  db.createSession()
}
