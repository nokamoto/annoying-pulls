package server

import github.GithubSetting
import play.api.Mode
import play.core.server.{NettyServer, ServerConfig}
import slack.SlackSetting
import scala.concurrent.duration._

class MockServers(port: Int) {
  val github = new GithubServer(port)

  val slack = new SlackServer(port)

  private[this] val config = ServerConfig(port = Some(port), mode = Mode.Test, address = "127.0.0.1")

  private[this] val server = NettyServer.fromRouter(config)(github.handler.orElse(slack.handler))

  def shutdown(): Unit = server.stop()
}

object MockServers {
  def withServers(f: (MockServers, GithubSetting, SlackSetting) => Unit): Unit = {
    val servers = new MockServers(9000)
    try {
      val gh = GithubSetting(
        api = servers.github.api,
        org = None,
        username = None,
        excludedLabels = Nil)

      val sl = SlackSetting(
        incomingWebhook = servers.slack.incomingWebhook,
        channel = None,
        username = None,
        iconEmoji = None,
        warningAfter = 7.days,
        dangerAfter = 14.days,
        attachmentsLimit = 20)

      f(servers, gh, sl)
    } finally {
      servers.shutdown()
    }
  }
}
