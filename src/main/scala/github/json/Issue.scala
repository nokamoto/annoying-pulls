package github.json

import java.time.ZonedDateTime

import play.api.libs.json.{Json, OFormat}

/**
  * @see [[https://developer.github.com/v3/issues/#get-a-single-issue]]
  */
case class Issue(labels: List[Label], created_at: String, user: User, comments: Long) {
  val createdAt: ZonedDateTime = ZonedDateTime.parse(created_at)
}

object Issue {
  implicit val format: OFormat[Issue] = Json.format[Issue]
}
