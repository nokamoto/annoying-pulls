package slack.json

import play.api.libs.json.{Json, OFormat}

/**
  * @see [[https://api.slack.com/incoming-webhooks]]
  */
case class Message(username: Option[String],
                   icon_emoji: Option[String],
                   channel: Option[String],
                   text: String,
                   attachments: List[Attachment])

object Message {
  implicit val format: OFormat[Message] = Json.format[Message]
}
