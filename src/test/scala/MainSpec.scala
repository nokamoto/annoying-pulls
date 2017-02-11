import java.time.ZonedDateTime

import MainSpec._
import github.Owner
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import server.{GithubOrg, GithubPull, GithubUser, MockServers}
import slack.json.Attachment

class MainSpec extends FlatSpec with ScalaFutures {
  private[this] implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  it should "post no pull requests to incoming webhook if :org and :username are not supplied" in {
    MockServers.withServers { (servers, defaultGh, defaultSl) =>
      whenReady(Main.run(defaultGh, defaultSl)) { _ =>
        val res = servers.slack.recevied.get()
        assert(res.text === "0 pull request opened")
        assert(res.attachments === Nil)
      }
    }
  }

  it should "post pull requests to incoming webhook" in {
    MockServers.withServers { (servers, defaultGh, defaultSl) =>
      val org = GithubOrg(owner = "org").update(_.repo("repo1", _.pull(1, now, const)))
      val user = GithubUser(owner = "user").update(_.repo("repo2", _.pull(2, now.minusSeconds(1), const)))

      servers.github.org.set(org)
      servers.github.user.set(user)

      val gh = defaultGh.copy(org = Some(Owner(org.owner)), username = Some(Owner(user.owner)))

      val expected = for {
        (owner, repo) <- org.repos.map(r => (org.owner, r)) ++ user.repos.map(r => (user.owner, r))
        pull <- repo.pulls
      } yield (pull, pull.attachment(owner = owner, repo = repo.name))

      whenReady(Main.run(gh, defaultSl)) { _ =>
        val res = servers.slack.recevied.get()
        assert(res.text === "2 pull requests opened")
        assert(res.attachments === expected.attachments)
      }
    }
  }
}

object MainSpec {
  private def const[A]: A => A = a => a

  private val now = ZonedDateTime.now()

  implicit class Update[A](a: A) {
    def update(f: A => A): A = f(a)
  }

  implicit class Attachments(s: Seq[(GithubPull, Attachment)]) {
    def attachments: List[Attachment] = {
      s.sortBy { case (pull, _) => pull.createdAt.toEpochSecond }.map { case (_, attachment) => attachment }.toList
    }
  }
}
