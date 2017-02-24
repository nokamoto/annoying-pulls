import java.time.ZonedDateTime
import java.util.UUID

import MainSpec._
import core.Context
import helper.DefaultFutures
import org.scalatest.FlatSpec
import server._
import slack.AttachmentColor
import slack.AttachmentColor.{Danger, Good, Warning}
import slack.json.{Attachment, IncomingWebhook}

import scala.concurrent.duration._

class MainSpec extends FlatSpec with DefaultFutures {
  private[this] val now = ZonedDateTime.now()

  private[this] def received(f: IncomingWebhook => Unit): Unit = {
    MockServers.withServers(now) { (servers, context) =>
      whenReady(Main.run(context)) { _ =>
        f(servers.slack.received.get())
      }
    }
  }

  private[this] def received(org: GithubOrg, user: GithubUser)
                            (f: (IncomingWebhook, Context) => Unit): Unit = {
    MockServers.withServers(now, org, user) { (servers, context) =>
      whenReady(Main.run(context)) { _ =>
        f(servers.slack.received.get(), context)
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

    received(org, user) { (message, context) =>
      val expected = pulls(context, org, user)

      assert(message.text === "4 pull requests opened")
      assert(message.attachments === expected.map(_.attachment))

      assert(expected.map(_.pull.number) === 2 :: 3 :: 4 :: 1 :: Nil)
    }
  }

  it should "post pull requests to incoming webhook with color" in {
    val dangerAfter = now.minusDays(14)
    val warningAfter = now.minusDays(7)

    val org = githubOrg(
      repo1 => repo1.pull(1, warningAfter.plusDays(1)).pull(2, dangerAfter.plusDays(1)))

    val user = githubUser(
      repo2 => repo2.pull(3, warningAfter).pull(4, dangerAfter))

    received(org, user) { (message, context) =>
      val expected = pulls(context, org, user)

      def colorNumbers(color: AttachmentColor) = expected.filter(_.attachment.color == color).map(_.pull.number)

      assert(message.text === "4 pull requests opened")
      assert(message.attachments === expected.map(_.attachment))

      assert(colorNumbers(Good) === 1L :: Nil)

      assert(context.slack.warningAfter === 7.days)
      assert(colorNumbers(Warning) === 2L :: 3L :: Nil)

      assert(context.slack.dangerAfter ===  14.days)
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

    received(org, user) { (message, context) =>
      val expected = pulls(context, org, user)

      assert(message.text === "3 pull requests opened (2 excluded)")

      assert(context.github.excludedLabels.contains(wontfix))
      assert(context.github.excludedLabels.contains(wip))
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

    received(org, user) { (message, context) =>
      val expected = pulls(context, org, user)

      assert(context.slack.attachmentsLimit === 20)
      assert(message.text === "30 pull requests opened (10 hidden)")
      assert(message.attachments === expected.map(_.attachment).take(context.slack.attachmentsLimit))
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

    received(org, user) { (message, context) =>
      val expected = pulls(context, org, user)

      assert(message.text === "30 pull requests opened (5 hidden, 5 excluded)")
      assert(message.attachments === expected.
        filterNot(_.pull.labels.contains(wontfix)).take(context.slack.attachmentsLimit).map(_.attachment))
    }
  }
}

object MainSpec {
  private case class PullAttachment(pull: GithubPull, attachment: Attachment)

  private[this] def update[A](a: A, f: A => A): A = f(a)

  private def githubOrg(fs: (GithubRepository => GithubRepository)*): GithubOrg = {
    fs.foldLeft(GithubOrg(owner = s"org-${UUID.randomUUID()}")) { case (org, f) =>
      update[GithubOrg](org, _.repo(s"repo-${UUID.randomUUID()}", f))
    }
  }

  private def githubUser(fs: (GithubRepository => GithubRepository)*): GithubUser = {
    fs.foldLeft(GithubUser(owner = s"user-${UUID.randomUUID()}")) { case (user, f) =>
      update[GithubUser](user, _.repo(s"repo-${UUID.randomUUID()}", f))
    }
  }

  private def pulls(context: Context, org: GithubOrg, user: GithubUser): List[PullAttachment] = {
    val res = for {
      repo <- org.repos ++ user.repos
      pull <- repo.pulls
    } yield PullAttachment(pull, pull.attachment(context))

    res.sortBy(_.pull.createdAt.toEpochSecond)
  }
}
