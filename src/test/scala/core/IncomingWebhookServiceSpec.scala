package core

import java.time.ZonedDateTime

import core.Context.StaticContext
import org.scalatest.FlatSpec
import slack.json.{Attachment, IncomingWebhook}
import core.IncomingWebhookServiceSpec._
import helper.DummyPullRequest

class IncomingWebhookServiceSpec extends FlatSpec with DummyPullRequest {
  import context._

  it should "make pulls with createdAt" in {
    val days = pullRequest(createdAt = now.minusDays(2))
    val day = pullRequest(createdAt = now.minusDays(1))
    val hours = pullRequest(createdAt = now.minusDays(1).plusSeconds(1))
    val hour = pullRequest(createdAt = now.minusHours(1))
    val minutes = pullRequest(createdAt = now.minusHours(1).plusSeconds(1))
    val minute = pullRequest(createdAt = now.minusMinutes(1))
    val seconds = pullRequest(createdAt = now.minusMinutes(1).plusSeconds(1))
    val second = pullRequest(createdAt = now.minusSeconds(1))
    val zero = pullRequest(createdAt = now)
    val minus = pullRequest(createdAt = now.plusSeconds(1))

    def expect(pull: PullRequest, expected: String): Unit = {
      withService(pull :: Nil) { actual =>
        assert(actual.attachments.head.footer === expected)
      }
    }

    expect(days, ":login opened 2 days ago")
    expect(day, ":login opened 1 day ago")
    expect(hours, ":login opened 23 hours ago")
    expect(hour, ":login opened 1 hour ago")
    expect(minutes, ":login opened 59 minutes ago")
    expect(minute, ":login opened 1 minute ago")
    expect(seconds, ":login opened 59 seconds ago")
    expect(second, ":login opened 1 second ago")
    expect(zero, ":login opened 0 second ago")
    expect(minus, ":login opened 0 second ago")
  }

  it should "make pulls with comments" in {
    val pull = pullRequest(createdAt = now, comments = 1)

    withService(pull :: Nil) { actual =>
      assert(actual.attachments.head.footer === s":login opened 0 second ago   ${slack.commentIconEmoji} 1")
    }
  }
}

object IncomingWebhookServiceSpec {
  private val context = new StaticContext {
    override val now: ZonedDateTime = ZonedDateTime.now()

    override val slack: SlackSetting = Context.slack

    override val github: GithubSetting = Context.github
  }

  private def incomingWebhook(text: String, attachments: List[Attachment]): IncomingWebhook = {
    import context.slack
    IncomingWebhook(
      username = slack.username,
      icon_emoji = slack.iconEmoji,
      channel = slack.channel,
      text = text,
      attachments = attachments)
  }

  private def withService(pulls: List[PullRequest])(f: IncomingWebhook => Unit): Unit = {
    val service = new IncomingWebhookService(context)
    f(service.incomingWebhook(pulls))
  }
}
