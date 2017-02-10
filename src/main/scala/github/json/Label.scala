package github.json

import play.api.libs.json.{Json, OFormat}

/**
  * @see [[https://developer.github.com/v3/issues/#get-a-single-issue]]
  */
case class Label(name: String)

object Label {
  implicit val format: OFormat[Label] = Json.format[Label]
}
