package github.json

import play.api.libs.json.{Json, OFormat}

/**
  * @see [[https://developer.github.com/v3/pulls/#list-pull-requests]]
  */
case class Pulls(url: String, html_url: String, title: String, issue_url: String, number: Long)

object Pulls {
  implicit val format: OFormat[Pulls] = Json.format[Pulls]
}
