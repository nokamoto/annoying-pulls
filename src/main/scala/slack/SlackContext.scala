package slack

/**
  * @see [[https://api.slack.com/incoming-webhooks]]
  */
case class SlackContext(incomingWebhook: String,
                        channel: Option[String],
                        username: Option[String],
                        iconEmoji: Option[String])
