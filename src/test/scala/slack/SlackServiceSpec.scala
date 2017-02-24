package slack

import java.time.ZonedDateTime
import java.util.UUID

import core._
import github.json.{Issue, Pull, Repo, User}
import helper.DefaultFutures
import org.scalatest.FlatSpec
import slack.SlackServiceSpec.{dummyPull, withService}

import scala.concurrent.ExecutionContext

/**
  * sbt "set testOptions in Test := Nil" "testOnly slack.SlackServiceSpec"
  */
class SlackServiceSpec extends FlatSpec with DefaultFutures {
  private[this] def post(pulls: Context => Seq[PullRequest]): Unit = {
    withService { (context, service) =>
      whenReady(service.webhook(pulls(context).toList))(_ => ())
    }
  }

  it should "post empty pulls to incoming webhook" taggedAs SlackTest in {
    post(_ => Nil)
  }

  it should "post 21 pulls to incoming webhook" taggedAs SlackTest in {
    post(context => (1 to 21).map(n => dummyPull(s"$n of 21", n, context.now)))
  }

  it should "post good, warning, danger pulls to incoming webhook" taggedAs SlackTest in {
    post { context =>
      import context._

      dummyPull("good", 1, now.minusDays(1)) ::
        dummyPull("warning", 2, now.minusDays(slack.warningAfter.toDays)) ::
        dummyPull("danger", 3, now.minusDays(slack.dangerAfter.toDays)) :: Nil
    }
  }

  it should "post pulls created before a day to incoming webhook" taggedAs SlackTest in {
    post { context =>
      import context._

      dummyPull("created at -23:59:59", 1, now.minusDays(1).plusSeconds(1)) ::
        dummyPull("created at -00:59:59", 2, now.minusHours(1).plusSeconds(1)) ::
        dummyPull("created at -00:00:59", 3, now.minusMinutes(1).plusSeconds(1)) ::
        dummyPull("created at -00:00:00", 4, now) ::
        dummyPull("(unexpected) created at +00:00:01", 5, now.plusSeconds(1)) :: Nil
    }
  }

  it should "post pulls with the number of comments to incoming webhook" taggedAs SlackTest in {
    post { context =>
      import context._

      dummyPull("0 comment", 1, now) ::
        dummyPull("1 comment", 2, now, 1) :: Nil
    }
  }
}

object SlackServiceSpec {
  def dummyPull(prefix: String, number: Long, createdAt: ZonedDateTime, comments: Long = 0): PullRequest = {
    val user = User(login = UUID.randomUUID().toString, avatar_url = "https://avatars.githubusercontent.com/u/4374383?v=3")

    PullRequest(
      repo = Repo(name = ":repo", full_name = ":owner/:repo"),
      pull = Pull(
        html_url = "https://localhost/:owner/:repo:/pulls/:number",
        title = s"$prefix - :title",
        issue_url = "https://localhost/:owner/:repo/issues/:number",
        number = number),
      issue = Issue(labels = Nil, created_at = createdAt.toString, user = user, comments = comments))
  }

  def withService(f: (Context, SlackService) => Unit)(implicit ec: ExecutionContext): Unit = {
    val context = Context()
    val service = new SlackService(context)
    try {
      f(context, service)
    } finally {
      context.shutdown()
    }
  }
}
