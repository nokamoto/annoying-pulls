package nokamoto

import java.time.ZonedDateTime

import org.scalatest.AsyncFlatSpec
import server._
import slack.AttachmentColor.{Danger, Good, Warning}

import scala.concurrent.duration._

class MainSpec extends AsyncFlatSpec {
  it should "post no pull requests to incoming webhook if :org and :username are not supplied" in {
    MockServersSetting().received { message =>
      assert(message.text === "0 pull request opened")
      assert(message.attachments === Nil)
    }
  }

  it should "post pull requests to incoming webhook ordered by created at" in {
    val now = ZonedDateTime.now()
    val setting = MockServersSetting(now = now,
                                     org = Some("mock-org"),
                                     user = Some("mock-user"))

    setting.setOrg(repo1 => repo1.pull(1, now),
                   repo2 => repo2.pull(2, now.minusDays(4)))

    setting.setUser(repo3 =>
      repo3.pull(3, now.minusDays(3)).pull(4, now.minusDays(2)))

    setting.received { message =>
      val expected = setting.pulls

      assert(message.text === "4 pull requests opened")
      assert(message.attachments === expected.map(_.attachment))

      assert(expected.map(_.pull.number) === 2 :: 3 :: 4 :: 1 :: Nil)
    }
  }

  it should "post pull requests to incoming webhook with color" in {
    val now = ZonedDateTime.now()
    val dangerAfter = 14.days
    val warningAfter = 7.days
    val setting = MockServersSetting(now = now,
                                     org = Some("mock-org"),
                                     dangerAfter = dangerAfter,
                                     warningAfter = warningAfter)

    setting.setOrg(
      repo =>
        repo
          .pull(1, now.minusDays(dangerAfter.toDays))
          .pull(2, now.minusDays(warningAfter.toDays))
          .pull(3, now))

    setting.received { message =>
      val expected = setting.pulls

      assert(message.text === "3 pull requests opened")
      assert(message.attachments === expected.map(_.attachment))

      assert(
        message.attachments.map(_.color) === Danger :: Warning :: Good :: Nil)
    }
  }

  it should "post pull requests to incoming webhook filtered by excluded labels" in {
    val wontfix = "wontfix"
    val bug = "bug"
    val setting = MockServersSetting(org = Some("mock-org"),
                                     excludedLabels = wontfix :: Nil)

    setting.setOrg(
      repo =>
        repo
          .pull(1, setting.now, _.labeled(wontfix))
          .pull(2, setting.now, _.labeled(bug)))

    setting.received { message =>
      val expected = setting.pulls.filterNot(_.pull.labels.contains(wontfix))

      assert(message.text === "2 pull requests opened (1 excluded)")
      assert(message.attachments === expected.map(_.attachment))
    }
  }

  it should "post pull requests to incoming webhook suppressed by attachments limit" in {
    val attachmentsLimit = 20
    val setting = MockServersSetting(org = Some("mock-org"),
                                     user = Some("mock-user"),
                                     attachmentsLimit = attachmentsLimit)

    def make(repo: GithubRepository): GithubRepository = {
      (1 to 15).foldLeft(repo) {
        case (r, n) =>
          r.pull(n, setting.now.minusDays(n))
      }
    }

    setting.setOrg(make)
    setting.setUser(make)

    setting.received { message =>
      val expected = setting.pulls.take(attachmentsLimit)

      assert(message.text === "30 pull requests opened (10 hidden)")
      assert(message.attachments === expected.map(_.attachment))
    }
  }

  it should "post pull requests to incoming webhook suppressed and filtered" in {
    val attachmentsLimit = 20
    val wontfix = "wontfix"
    val setting = MockServersSetting(org = Some("mock-org"),
                                     excludedLabels = wontfix :: Nil,
                                     attachmentsLimit = attachmentsLimit)

    setting.setOrg { repo =>
      (1 to 30).foldLeft(repo) {
        case (r, n) =>
          if (n <= 5) {
            r.pull(n, setting.now.minusDays(n), _.labeled(wontfix))
          } else {
            r.pull(n, setting.now.minusDays(n))
          }
      }
    }

    setting.received { message =>
      val expected = setting.pulls.take(attachmentsLimit)

      assert(message.text === "30 pull requests opened (5 hidden, 5 excluded)")
      assert(message.attachments === expected.map(_.attachment))
    }
  }
}
