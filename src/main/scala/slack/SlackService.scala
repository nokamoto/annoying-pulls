package slack

import core.{CoreContext, PullRequest, PrettyOps}
import play.api.libs.json.{JsString, Json}
import slack.json.Attachment

import scala.concurrent.Future

class SlackService(sl: SlackSetting, core: CoreContext) {
  import core._

  private[this] def postAttachments(text: String, attachments: List[Attachment]) = {
    val options = ("username" -> sl.username) :: ("icon_emoji" -> sl.iconEmoji) :: ("channel" -> sl.channel) :: Nil

    val default = Json.obj("text" -> text, "attachments" -> attachments)

    val json = options.foldLeft(default) { case (j, (k, v)) =>
      v.map(s => j + (k -> JsString(s))).getOrElse(j)
    }

    println(sl.incomingWebhook)
    ws.url(sl.incomingWebhook).post(json).map { res =>
      println(s"${res.status} - ${res.body}")
    }
  }

  private[this] def color(p: PullRequest): String = {
    p.daysAgo match {
      case ago if ago >= sl.dangerAfter => "danger"
      case ago if ago >= sl.warningAfter => "warning"
      case _ => "good"
    }
  }

  private[this] def attachment(p: PullRequest): Attachment = {
    Attachment(
      title = s"[${p.repo.full_name}] ${p.pull.title} #${p.pull.number}",
      title_link = p.pull.html_url,
      footer = p.prettyDays,
      color = color(p))
  }

  /**
    * post the pull requests attachments to the slack incoming webhook.
    */
  def webhook(pulls: List[PullRequest]): Future[Unit] = {
    val size = pulls.size
    val text = s"$size pull request opened ${PrettyOps.s(size)}"
    val hidden = if (size > sl.attachmentsLimit) s" (${size - sl.attachmentsLimit} hidden)" else ""
    postAttachments(
      text + hidden,
      pulls.sortBy(_.createdAt.toEpochSecond).take(sl.attachmentsLimit).map(attachment))
  }
}
