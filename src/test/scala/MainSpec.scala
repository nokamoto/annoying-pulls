import java.time.ZonedDateTime
import java.util.UUID

import MainSpec._
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import server._
import slack.json.{Attachment, Message}

class MainSpec extends FlatSpec with ScalaFutures {
  private[this] implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  private[this] def received(f: Message => Unit): Unit = {
    MockServers.withServers { (servers, gh, sl) =>
      whenReady(Main.run(gh, sl)) { _ =>
        f(servers.slack.received.get())
      }
    }
  }

  private[this] def received(org: GithubOrg, user: GithubUser)(f: Message => Unit): Unit = {
    MockServers.withServers(org, user) { (servers, gh, sl) =>
      whenReady(Main.run(gh, sl)) { _ =>
        f(servers.slack.received.get())
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

    val expected = pulls(org, user)

    received(org, user) { message =>
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
    pending
  }

  it should "post pull requests to incoming webhook filtered by excluded labels" in {
    pending
  }

  it should "post pull requests to incoming webhook surpressed by attachments limit" in {
    pending
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

  private val now = ZonedDateTime.now()

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

  private def pulls(org: GithubOrg, user: GithubUser): List[PullAttachment] = {
    val res = for {
      (owner, repo) <- org.ownerRepos ++ user.ownerRepos
      pull <- repo.pulls
    } yield PullAttachment(pull, pull.attachment(owner = owner, repo = repo.name))
    res.sortBy(_.pull.createdAt.toEpochSecond)
  }
}
