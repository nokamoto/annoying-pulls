package slack

import play.api.data.validation.ValidationError
import play.api.libs.json._

sealed abstract class AttachmentColor(val value: String)

object AttachmentColor {
  case object Good extends AttachmentColor("good")
  case object Warning extends AttachmentColor("warning")
  case object Danger extends AttachmentColor("danger")

  val values = Set(Good, Warning, Danger)

  implicit val format: Format[AttachmentColor] = {
    new Format[AttachmentColor] {
      override def reads(json: JsValue): JsResult[AttachmentColor] =
        json.validate[String].flatMap { s =>
          values
            .find(_.value == s)
            .map(JsSuccess(_))
            .getOrElse(JsError(ValidationError(s"$s undefined")))
        }

      override def writes(o: AttachmentColor): JsValue = JsString(o.value)
    }
  }
}
