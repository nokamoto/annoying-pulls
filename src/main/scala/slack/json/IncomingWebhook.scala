package slack.json

import play.api.libs.json.{Json, OFormat}

/**
  * @see [[https://api.slack.com/incoming-webhooks]]
  */
case class IncomingWebhook(username: Option[String],
                           icon_emoji: Option[String],
                           channel: Option[String],
                           text: String,
                           attachments: List[Attachment])

object IncomingWebhook {
  implicit val format: OFormat[IncomingWebhook] = Json.format[IncomingWebhook]
}
