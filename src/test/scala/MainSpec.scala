import java.time.ZonedDateTime
import java.util.UUID

import MainSpec._
import akka.actor.ActorSystem
import core.CoreContext
import github.GithubSetting
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import server._
import slack.AttachmentColor.{Danger, Good, Warning}
import slack.{AttachmentColor, SlackSetting}
import slack.json.{Attachment, IncomingWebhook}

import scala.concurrent.duration._

class MainSpec extends FlatSpec with ScalaFutures with BeforeAndAfterAll {
  private[this] implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  private[this] val core = new CoreContext(system = ActorSystem())

  private[this] val now = core.now

  private[this] def received(f: IncomingWebhook => Unit): Unit = {
    MockServers.withServers { (servers, gh, sl) =>
      whenReady(Main.run(core, gh, sl)) { _ =>
        f(servers.slack.received.get())
      }
    }
  }

  private[this] def received(org: GithubOrg, user: GithubUser)
                            (f: (IncomingWebhook, GithubSetting, SlackSetting) => Unit): Unit = {
    MockServers.withServers(org, user) { (servers, gh, sl) =>
      whenReady(Main.run(core, gh, sl)) { _ =>
        f(servers.slack.received.get(), gh, sl)
      }
    }
  }

  it should "post no pull requests to incoming webhook if :org and :username are not supplied" in {
    received { message =>
      assert(message.text === "0 pull request opened")
      assert(message.attachments === Nil)
    }
  }

  it should "post pull requests to incoming webhook ordered by created at" in {
    val org = githubOrg(
      repo1 => repo1.pull(1, now),
      repo2 => repo2.pull(2, now.minusDays(4)))

    val user = githubUser(
      repo3 => repo3.pull(3, now.minusDays(3)).pull(4, now.minusDays(2)))

    val expected = pulls(now, org, user)

    received(org, user) { (message, _, _) =>
      assert(message.text === "4 pull requests opened")
      assert(message.attachments === expected.map(_.attachment))

      expected.foldLeft(0L) { case (last, cur) =>
        val sec = cur.pull.createdAt.toEpochSecond
        assert(last <= sec)
        sec
      }
    }
  }

  it should "post pull requests to incoming webhook with color" in {
    val dangerAfter = now.minusDays(14)
    val warningAfter = now.minusDays(7)

    val org = githubOrg(
      repo1 => repo1.pull(1, warningAfter.plusDays(1)).pull(2, dangerAfter.plusDays(1)))

    val user = githubUser(
      repo2 => repo2.pull(3, warningAfter).pull(4, dangerAfter))

    val expected = pulls(now, org, user)

    def colorNumbers(color: AttachmentColor) = expected.filter(_.attachment.color == color).map(_.pull.number)

    received(org, user) { (message, _, slack) =>
      assert(message.text === "4 pull requests opened")
      assert(message.attachments === expected.map(_.attachment))

      assert(colorNumbers(Good) === 1L :: Nil)

      assert(slack.warningAfter === 7.days)
      assert(colorNumbers(Warning) === 2L :: 3L :: Nil)

      assert(slack.dangerAfter ===  14.days)
      assert(colorNumbers(Danger) === 4L :: Nil)
    }
  }

  it should "post pull requests to incoming webhook filtered by excluded labels" in {
    val wontfix = "wontfix"
    val wip = "wip"
    val bug = "bug"

    val org = githubOrg(repo =>
      repo.pull(1, now, _.labeled(wontfix)).pull(2, now, _.labeled(wip)).pull(3, now, _.labeled(bug)))

    val user = githubUser(empty => empty)

    val expected = pulls(now, org, user)

    received(org, user) { (message, github, _) =>
      assert(message.text === "3 pull requests opened (2 excluded)")

      assert(github.excludedLabels.contains(wontfix))
      assert(github.excludedLabels.contains(wip))
      assert(message.attachments === expected.filter(_.pull.labels.contains(bug)).map(_.attachment))
    }
  }

  it should "post pull requests to incoming webhook suppressed by attachments limit" in {
    val org = githubOrg { repo =>
      (0 until 30).zipWithIndex.foldLeft(repo) { case (r, (daysAgo, number)) =>
        r.pull(number, now.minusDays(daysAgo))
      }
    }

    val user = githubUser(empty => empty)

    val expected = pulls(now, org, user)

    received(org, user) { (message, _, slack) =>
      assert(slack.attachmentsLimit === 20)
      assert(message.text === "30 pull requests opened (10 hidden)")
      assert(message.attachments === expected.map(_.attachment).take(slack.attachmentsLimit))
    }
  }

  it should "post pull requests to incoming webhook suppressed and filtered" in {
    val wontfix = "wontfix"

    val org = githubOrg { repo =>
      (0 until 30).zipWithIndex.foldLeft(repo) { case (r, (daysAgo, number)) =>
        if (number < 5) {
          r.pull(number, now.minusDays(daysAgo), _.labeled(wontfix))
        } else {
          r.pull(number, now.minusDays(daysAgo))
        }
      }
    }

    val user = githubUser(empty => empty)

    val expected = pulls(now, org, user)

    received(org, user) { (message, _, slack) =>
      assert(message.text === "30 pull requests opened (5 hidden, 5 excluded)")
      assert(message.attachments === expected.
        filterNot(_.pull.labels.contains(wontfix)).take(slack.attachmentsLimit).map(_.attachment))
    }
  }

  override protected def afterAll(): Unit = {
    core.shutdown()
    super.afterAll()
  }
}

object MainSpec {
  private case class PullAttachment(pull: GithubPull, attachment: Attachment)

  private[this] implicit class Update[A](a: A) {
    def update(f: A => A): A = f(a)
  }

  private[this] implicit class Attachments(s: Seq[(GithubPull, Attachment)]) {
    def attachments: List[Attachment] = {
      s.sortBy { case (pull, _) => pull.createdAt.toEpochSecond }.map { case (_, attachment) => attachment }.toList
    }
  }

  private def githubOrg(fs: (GithubRepository => GithubRepository)*): GithubOrg = {
    fs.foldLeft(GithubOrg(owner = s"org-${UUID.randomUUID()}")) { case (org, f) =>
      org.update(_.repo(s"repo-${UUID.randomUUID()}", f))
    }
  }

  private def githubUser(fs: (GithubRepository => GithubRepository)*): GithubUser = {
    fs.foldLeft(GithubUser(owner = s"user-${UUID.randomUUID()}")) { case (user, f) =>
      user.update(_.repo(s"repo-${UUID.randomUUID()}", f))
    }
  }

  private def pulls(now: ZonedDateTime, org: GithubOrg, user: GithubUser): List[PullAttachment] = {
    val res = for {
      repo <- org.repos ++ user.repos
      pull <- repo.pulls
    } yield PullAttachment(pull, pull.attachment(now))

    res.sortBy(_.pull.createdAt.toEpochSecond)
  }
}
