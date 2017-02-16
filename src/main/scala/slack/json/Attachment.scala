package slack.json

import play.api.libs.json.{Json, OFormat}
import slack.AttachmentColor

/**
  * @see [[https://api.slack.com/docs/message-attachments]]
  */
case class Attachment(title: String, title_link: String, footer: String, footer_icon: String, color: AttachmentColor)

object Attachment {
  implicit val format: OFormat[Attachment] = Json.format[Attachment]
}
