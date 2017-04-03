package server

import java.net.ServerSocket

import play.api.Mode
import play.core.server.{NettyServer, ServerConfig}

import scala.collection.mutable

class MockServers(pageSize: Int) {
  val port: Int = {
    def acquire(): Int = {
      val socket = new ServerSocket(0)
      try {
        if (MockServers.reserve(socket.getLocalPort)) {
          socket.getLocalPort
        } else {
          acquire()
        }
      } finally {
        socket.close()
      }
    }
    acquire()
  }

  val github = new GithubServer(port, pageSize)

  val slack = new SlackServer(port)

  private[this] val config =
    ServerConfig(port = Some(port), mode = Mode.Test, address = "127.0.0.1")

  private[this] val server =
    NettyServer.fromRouter(config)(github.handler.orElse(slack.handler))

  def shutdown(): Unit = server.stop()
}

object MockServers {
  private[this] val reservedPorts = mutable.Set.empty[Int]

  def reserve(port: Int): Boolean = synchronized {
    reservedPorts.add(port)
  }
}
