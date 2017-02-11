package server

import play.api.Mode
import play.core.server.{NettyServer, ServerConfig}

class MockServers(port: Int) {
  val github = new GithubServer(port)

  val slack = new SlackServer(port)

  private[this] val config = ServerConfig(port = Some(port), mode = Mode.Test, address = "127.0.0.1")

  private[this] val server = NettyServer.fromRouter(config)(github.handler.orElse(slack.handler))

  def shutdown(): Unit = server.stop()
}
