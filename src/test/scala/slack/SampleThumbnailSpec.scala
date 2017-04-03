package slack

import java.time.ZonedDateTime

import core.Context
import helper.{DefaultFutures, DummyPullRequest}
import org.scalatest.FlatSpec

/**
  * sbt "set testOptions in Test := Nil" "testOnly slack.SampleThumbnailSpec"
  */
class SampleThumbnailSpec
    extends FlatSpec
    with DefaultFutures
    with DummyPullRequest {
  it should "post a sample message for thumbnail" taggedAs SlackTest in {
    val context =
      new Context(now = ZonedDateTime.now(),
                  github = Context.github,
                  slack = Context.slack.copy(iconEmoji = Some(":octocat:")))

    val service = new SlackService(context)

    import context._

    val pulls = pullRequest(createdAt = now.minusDays(1), number = 12) ::
      pullRequest(createdAt = now.minusDays(slack.warningAfter.toDays),
                  number = 11,
                  comments = 8) ::
      pullRequest(createdAt = now.minusDays(slack.dangerAfter.toDays),
                  number = 10,
                  comments = 32) :: Nil

    try {
      whenReady(service.webhook(pulls))(_ => ())
    } finally {
      context.shutdown()
    }
  }
}
