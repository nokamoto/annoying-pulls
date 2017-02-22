package core

import github.GithubSetting
import slack.SlackSetting
import slack.json.IncomingWebhook

class IncomingWebhookService(core: CoreContext, gh: GithubSetting, sl: SlackSetting) {
  private[this] def exclude(pulls: List[PullRequest]) = {
    pulls.filterNot(_.issue.labels.exists(label => gh.excludedLabels.contains(label.name)))
  }

  private[this] def print(cond: Boolean, s: String): String = if (cond) s else ""

  private[this] def text(pulls: List[PullRequest]): String = {
    val size = pulls.size
    val excludedSize = exclude(pulls).size

    val text = s"$size pull request${PrettyOps.s(size)} opened"

    val hidden = print(excludedSize > sl.attachmentsLimit, s"${excludedSize - sl.attachmentsLimit} hidden")
    val excluded = print(size > excludedSize, s"${size - excludedSize} excluded")
    val extended = hidden + print(hidden.nonEmpty && excluded.nonEmpty, ", ") + print(excluded.nonEmpty, excluded)

    text + print(extended.nonEmpty, s" ($extended)")
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
      attachments = exclude(pulls).
        sortBy(_.createdAt.toEpochSecond).
        take(sl.attachmentsLimit).
        map(_.like.attachment(core.now).make(warningAfter = sl.warningAfter, dangerAfter = sl.dangerAfter)))
  }
}
