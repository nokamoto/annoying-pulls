package server

import github.GithubSetting
import play.api.Mode
import play.core.server.{NettyServer, ServerConfig}
import slack.SlackSetting

class MockServers(port: Int) {
  val github = new GithubServer(port)

  val slack = new SlackServer(port)

  private[this] val config = ServerConfig(port = Some(port), mode = Mode.Test, address = "127.0.0.1")

  private[this] val server = NettyServer.fromRouter(config)(github.handler.orElse(slack.handler))

  def shutdown(): Unit = server.stop()
}

object MockServers {
  private[this] def withServers(org: Option[GithubOrg],
                                user: Option[GithubUser],
                                f: (MockServers, GithubSetting, SlackSetting) => Unit): Unit = {
    val servers = new MockServers(9000)
    try {
      val gh = GithubSetting(
        api = servers.github.api,
        org = org.map(_.toOwner),
        username = user.map(_.toOwner),
        excludedLabels = Nil)

      val sl = SlackSetting(
        incomingWebhook = servers.slack.incomingWebhook,
        channel = None,
        username = None,
        iconEmoji = None,
        warningAfter = GithubPull.warningAfter,
        dangerAfter = GithubPull.dangerAfter,
        attachmentsLimit = 20)

      org.foreach(servers.github.org.set)
      user.foreach(servers.github.user.set)

      f(servers, gh, sl)
    } finally {
      servers.shutdown()
    }
  }

  def withServers(f: (MockServers, GithubSetting, SlackSetting) => Unit): Unit = withServers(None, None, f)

  def withServers(org: GithubOrg, user: GithubUser)(f: (MockServers, GithubSetting, SlackSetting) => Unit): Unit = {
    withServers(Some(org), Some(user), f)
  }
}
