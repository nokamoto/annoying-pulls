package slack

import core.{CoreContext, PrettyOps, PullRequest}
import play.api.libs.json.Json
import slack.AttachmentColor.{Danger, Good, Warning}
import slack.json.{Attachment, Message}

import scala.concurrent.{ExecutionContext, Future}

class SlackService(sl: SlackSetting, core: CoreContext)(implicit ec: ExecutionContext) {
  import core._

  private[this] def postAttachments(text: String, attachments: List[Attachment]) = {
    val json = Message(username = sl.username, icon_emoji = sl.iconEmoji, channel = sl.channel, text = text, attachments = attachments)

    println(sl.incomingWebhook)

    ws.url(sl.incomingWebhook).post(Json.toJson(json)).map { res =>
      println(s"${res.status} - ${res.body}")
    }
  }

  private[this] def color(p: PullRequest): AttachmentColor = {
    p.daysAgo match {
      case ago if ago >= sl.dangerAfter => Danger
      case ago if ago >= sl.warningAfter => Warning
      case _ => Good
    }
  }

  private[this] def attachment(p: PullRequest): Attachment = {
    Attachment(
      title = p.attachmentTitle,
      title_link = p.pull.html_url,
      footer = p.prettyDays,
      color = color(p))
  }

  /**
    * post the pull requests attachments to the slack incoming webhook.
    */
  def webhook(pulls: List[PullRequest]): Future[Unit] = {
    val size = pulls.size
    val text = s"$size pull request${PrettyOps.s(size)} opened"
    val hidden = if (size > sl.attachmentsLimit) s" (${size - sl.attachmentsLimit} hidden)" else ""
    postAttachments(
      text + hidden,
      pulls.sortBy(_.createdAt.toEpochSecond).take(sl.attachmentsLimit).map(attachment))
  }
}
