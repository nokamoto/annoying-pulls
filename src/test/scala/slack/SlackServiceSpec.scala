package slack

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import core.{CoreContext, IncomingWebhookService, PullRequest}
import github.GithubSetting
import github.json.{Issue, Pull, Repo, User}
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.ExecutionContext.Implicits.global
import slack.SlackServiceSpec.{dummyPull, settings}

/**
  * sbt "set testOptions in Test := Nil" "testOnly slack.SlackServiceSpec"
  */
class SlackServiceSpec extends FlatSpec with ScalaFutures {
  private[this] implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  private[this] def post(service: SlackService, core: CoreContext, pulls: Seq[PullRequest]): Unit = {
    whenReady(service.webhook(pulls.toList).andThen { case _ => core.shutdown() })(_ => ())
  }

  it should "post empty pulls to incoming webhook" taggedAs SlackTest in {
    val (_, core, service) = settings
    val pulls = Nil
    post(service, core, pulls)
  }

  it should "post 21 pulls to incoming webhook" taggedAs SlackTest in {
    val (now, core, service) = settings
    val pulls = (1 to 21).map(n => dummyPull(s"$n of 21", n, now))
    post(service, core, pulls)
  }

  it should "post good, warning, danger pulls to incoming webhook" taggedAs SlackTest in {
    val (now, core, service) = settings
    val pulls =
      dummyPull("good", 1, now) ::
      dummyPull("warning", 2, now.minusDays(7)) ::
      dummyPull("danger", 3, now.minusDays(14)) :: Nil
    post(service, core, pulls)
  }
}

object SlackServiceSpec {
  def dummyPull(prefix: String, number: Long, createdAt: ZonedDateTime): PullRequest = {
    val user = User(login = UUID.randomUUID().toString, avatar_url = "https://avatars.githubusercontent.com/u/4374383?v=3")

    PullRequest(
      repo = Repo(name = ":repo", full_name = ":owner/:repo"),
      pull = Pull(
        html_url = "https://localhost/:owner/:repo:/pulls/:number",
        title = s"$prefix - :title",
        issue_url = "https://localhost/:owner/:repo/issues/:number",
        number = number),
      issue = Issue(labels = Nil, created_at = createdAt.toString, user = user))
  }

  def settings: (ZonedDateTime, CoreContext, SlackService) = {
    val now = ZonedDateTime.now()
    val config = ConfigFactory.load()
    val sl = SlackSetting(config.getConfig("slack"))
    val gh = GithubSetting(config.getConfig("github"))
    val core = new CoreContext(system = ActorSystem())
    val iw = new IncomingWebhookService(gh = gh, sl = sl)
    val service = new SlackService(sl = sl, core = core, service = iw)
    (now, core, service)
  }
}
