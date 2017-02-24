package core

import slack.json.IncomingWebhook

class IncomingWebhookService(context: Context) {
  import context._

  private[this] def exclude(pulls: List[PullRequest]) = {
    pulls.filterNot(_.issue.labels.exists(label => github.excludedLabels.contains(label.name)))
  }

  private[this] def print(cond: Boolean, s: String): String = if (cond) s else ""

  private[this] def text(pulls: List[PullRequest]): String = {
    val size = pulls.size
    val excludedSize = exclude(pulls).size

    val text = s"$size pull request${PrettyOps.s(size)} opened"

    val hidden = print(excludedSize > slack.attachmentsLimit, s"${excludedSize - slack.attachmentsLimit} hidden")
    val excluded = print(size > excludedSize, s"${size - excludedSize} excluded")
    val extended = hidden + print(hidden.nonEmpty && excluded.nonEmpty, ", ") + print(excluded.nonEmpty, excluded)

    text + print(extended.nonEmpty, s" ($extended)")
  }

  /**
    * convert the [[PullRequest]] list to [[IncomingWebhook]].
    */
  def incomingWebhook(pulls: List[PullRequest]): IncomingWebhook = {
    IncomingWebhook(
      username = slack.username,
      icon_emoji = slack.iconEmoji,
      channel = slack.channel,
      text = text(pulls),
      attachments = exclude(pulls).
        sortBy(_.createdAt.toEpochSecond).
        take(slack.attachmentsLimit).
        map(_.like.attachment(now).make(warningAfter = slack.warningAfter, dangerAfter = slack.dangerAfter)))
  }
}
