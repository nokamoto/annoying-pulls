package core

import github.GithubSetting
import slack.SlackSetting
import slack.json.{Attachment, IncomingWebhook}

class IncomingWebhookService(gh: GithubSetting, sl: SlackSetting) {
  private[this] def text(pulls: List[PullRequest]): String = {
    val size = pulls.size
    val text = s"$size pull request${PrettyOps.s(size)} opened"
    val hidden = if (size > sl.attachmentsLimit) s" (${size - sl.attachmentsLimit} hidden)" else ""
    text + hidden
  }

  private[this] def attachment(pull: PullRequest): Attachment = {
    Attachment(
      title = pull.attachmentTitle,
      title_link = pull.pull.html_url,
      footer = pull.prettyDays,
      color = pull.color(warningAfter = sl.warningAfter, dangerAfter = sl.dangerAfter))
  }

  /**
    * convert the [[PullRequest]] list to [[IncomingWebhook]].
    */
  def incomingWebhook(pulls: List[PullRequest]): IncomingWebhook = {
    IncomingWebhook(
      username = sl.username,
      icon_emoji = sl.iconEmoji,
      channel = sl.channel,
      text = text(pulls),
      attachments = pulls.sortBy(_.createdAt.toEpochSecond).take(sl.attachmentsLimit).map(attachment))
  }
}
