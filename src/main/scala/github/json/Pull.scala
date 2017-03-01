package github.json

import play.api.libs.json.{Json, OFormat}

/**
  * @see [[https://developer.github.com/v3/pulls/#get-a-single-pull-request]]
  */
case class Pull(comments: Long, review_comments: Long)

object Pull {
  implicit val format: OFormat[Pull] = Json.format[Pull]
}
