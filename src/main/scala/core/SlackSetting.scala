package core

import com.typesafe.config.Config
import core.ConfigOps.Implicits.Ops

import scala.concurrent.duration.FiniteDuration

/**
  * @param warningAfter an attachment with warning color if the pull request is opened before this.
  * @param dangerAfter an attachment with danger color if the pull request is opened before this.
  * @param attachmentsLimit suppress the number of attachments.
  * @param commentIconEmoji print as a comment icon in the attachment footer.
  *
  * @see [[https://api.slack.com/incoming-webhooks]]
  */
case class SlackSetting(incomingWebhook: String,
                        channel: Option[String],
                        username: Option[String],
                        iconEmoji: Option[String],
                        warningAfter: FiniteDuration,
                        dangerAfter: FiniteDuration,
                        attachmentsLimit: Int,
                        commentIconEmoji: String)

object SlackSetting {
  def apply(config: Config): SlackSetting = {
    SlackSetting(
      incomingWebhook = config.getString("incoming-webhook"),
      channel = config.getOptionString("channel"),
      username = config.getOptionString("username"),
      iconEmoji = config.getOptionString("icon-emoji"),
      warningAfter = config.getFiniteDuration("warning-after"),
      dangerAfter = config.getFiniteDuration("danger-after"),
      attachmentsLimit = config.getInt("attachments-limit"),
      commentIconEmoji = config.getString("comment-icon-emoji"))
  }
}
