package slack.json

import play.api.libs.json.{Json, OFormat}

/**
  * @see [[https://api.slack.com/docs/message-attachments]]
  */
case class Attachment(title: String, title_link: String, footer: String, color: String)

object Attachment {
  implicit val format: OFormat[Attachment] = Json.format[Attachment]
}
