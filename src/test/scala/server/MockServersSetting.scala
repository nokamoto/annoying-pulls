package server

import java.time.ZonedDateTime
import java.util.UUID

import core.{Context, GithubSetting, SlackSetting}
import github.{Owner, OwnerRepo}
import nokamoto.Main
import org.scalatest.Assertion
import server.MockServersSetting.PullAttachment
import slack.json.{Attachment, IncomingWebhook}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

case class MockServersSetting(now: ZonedDateTime = ZonedDateTime.now(),
                              org: Option[String] = None,
                              user: Option[String] = None,
                              excludedLabels: List[String] = Nil,
                              personalAccessToken: Option[String] = None,
                              includeRepos: List[OwnerRepo] = Nil,
                              warningAfter: FiniteDuration = 7.days,
                              dangerAfter: FiniteDuration = 14.days,
                              attachmentsLimit: Int = 20,
                              pageSize: Int = 100) {

  private[server] val servers = new MockServers(pageSize)

  private[this] val github = GithubSetting(
    api = servers.github.api,
    org = org.map(Owner),
    username = user.map(Owner),
    excludedLabels = excludedLabels,
    personalAccessToken = personalAccessToken,
    includeRepos = includeRepos
  )

  private[this] val slack = SlackSetting(
    incomingWebhook = servers.slack.incomingWebhook,
    channel = None,
    username = None,
    iconEmoji = None,
    warningAfter = warningAfter,
    dangerAfter = dangerAfter,
    attachmentsLimit = attachmentsLimit,
    commentIconEmoji = ":speech_balloon:"
  )

  val context = new Context(now = now, github = github, slack = slack)

  def forceSetOrg(name: String,
                  fs: (GithubRepository => GithubRepository)*): Unit = {
    val res = fs.foldLeft(GithubOrg(owner = name)) {
      case (o, f) =>
        o.repo(s"repo-${UUID.randomUUID()}", f)
    }
    servers.github.org.set(res)
  }

  def setOrg(fs: (GithubRepository => GithubRepository)*): Unit = {
    org.foreach(s => forceSetOrg(s, fs: _*))
  }

  def setUser(fs: (GithubRepository => GithubRepository)*): Unit = {
    user.foreach { s =>
      val res = fs.foldLeft(GithubUser(owner = s)) {
        case (u, f) =>
          u.repo(s"repo-${UUID.randomUUID()}", f)
      }
      servers.github.user.set(res)
    }
  }

  def pulls: List[PullAttachment] = {
    val o = Option(servers.github.org.get())
    val u = Option(servers.github.user.get())

    val res = for {
      repo <- (o.map(_.repos).toList ++ u.map(_.repos).toList).flatten
      pull <- repo.pulls
    } yield PullAttachment(pull, pull.attachment(context))

    res.sortBy(_.pull.createdAt.toEpochSecond)
  }

  def withServer[A](f: => Future[A])(
      implicit ec: ExecutionContext): Future[A] = {
    f.andThen {
      case _ =>
        context.shutdown()
        servers.shutdown()
    }
  }

  def received(f: IncomingWebhook => Assertion)(
      implicit ec: ExecutionContext): Future[Assertion] = {
    withServer(Main.run(context).map(_ => f(servers.slack.received.get())))
  }
}

object MockServersSetting {
  case class PullAttachment(pull: GithubPull, attachment: Attachment)
}
