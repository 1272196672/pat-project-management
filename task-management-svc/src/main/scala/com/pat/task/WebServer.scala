package com.pat.task

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util._

object WebServer {
  def start(httpHost: String, httpPort: Int, routes: Route)(implicit system: ActorSystem[_]): Unit = {
    implicit val executionContext: ExecutionContextExecutor = system.executionContext
    Http()(system)
      .newServerAt(httpHost, httpPort)
      .bind(routes)
      .map(_.addToCoordinatedShutdown(3 seconds)(system))
      .onComplete {
        case Success(binding) =>
          val address = binding.localAddress
          system.log.info(s"WebServer started at http://${address.getHostString}:${address.getPort}")
        case Failure(ex) =>
          system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
          system.terminate()
      }
  }
}
