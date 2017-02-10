package github.json

import play.api.libs.json.{Json, OFormat}

/**
  * @see [[https://developer.github.com/v3/pulls/#list-pull-requests]]
  */
case class Pull(html_url: String, title: String, issue_url: String, number: Long)

object Pull {
  implicit val format: OFormat[Pull] = Json.format[Pull]
}
