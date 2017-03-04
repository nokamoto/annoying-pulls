package server

import java.time.ZonedDateTime

import core.{Context, GithubSetting, SlackSetting}
import play.api.Mode
import play.core.server.{NettyServer, ServerConfig}

import scala.concurrent.duration._

class MockServers(port: Int) {
  val github = new GithubServer(port)

  val slack = new SlackServer(port)

  private[this] val config = ServerConfig(port = Some(port), mode = Mode.Test, address = "127.0.0.1")

  private[this] val server = NettyServer.fromRouter(config)(github.handler.orElse(slack.handler))

  def shutdown(): Unit = server.stop()
}

object MockServers {
  private[this] def withServers(now: ZonedDateTime,
                                org: Option[GithubOrg],
                                user: Option[GithubUser],
                                f: (MockServers, Context) => Unit): Unit = {
    val servers = new MockServers(9000)

    val github = GithubSetting(
      api = servers.github.api,
      org = org.map(_.toOwner),
      username = user.map(_.toOwner),
      excludedLabels = "wontfix" :: "wip" :: Nil,
      personalAccessToken = None)

    val slack = SlackSetting(
      incomingWebhook = servers.slack.incomingWebhook,
      channel = None,
      username = None,
      iconEmoji = None,
      warningAfter = 7.days,
      dangerAfter = 14.days,
      attachmentsLimit = 20,
      commentIconEmoji = ":speech_balloon:")

    val context = new Context(now = now, github = github, slack = slack)

    try {
      org.foreach(servers.github.org.set)
      user.foreach(servers.github.user.set)
      f(servers, context)
    } finally {
      context.shutdown()
      servers.shutdown()
    }
  }

  def withServers(now: ZonedDateTime)(f: (MockServers, Context) => Unit): Unit = withServers(now, None, None, f)

  def withServers(now: ZonedDateTime, org: GithubOrg, user: GithubUser)(f: (MockServers, Context) => Unit): Unit = {
    withServers(now, Some(org), Some(user), f)
  }
}
