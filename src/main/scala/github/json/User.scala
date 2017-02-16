package github.json

import play.api.libs.json.{Json, OFormat}

/**
  * @see [[https://developer.github.com/v3/issues/#get-a-single-issue]]
  */
case class User(login: String, avatar_url: String)

object User {
  implicit val format: OFormat[User] = Json.format[User]
}
