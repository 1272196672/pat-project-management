package com.pat.task

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route

import java.net.InetSocketAddress
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util._

object WebServer {
  var binding: Future[ServerBinding] = null
  def start(httpHost: String, httpPort: Int, routes: Route)(implicit system: ActorSystem[_]): Unit = {
    implicit val executionContext: ExecutionContextExecutor = system.executionContext
    val binding = Http()(system)
      .newServerAt(httpHost, httpPort)
      .bind(routes)
    binding
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

  def stop(implicit system: ActorSystem[_]): Unit = {
    Await.result(binding, 5 seconds).terminate(3 seconds)
    system.log.info(s"WebServer terminate!")
  }
}
