package github.json

import play.api.libs.json.{Json, OFormat}

/**
  * @see [[https://developer.github.com/v3/repos/#list-organization-repositories]]
  */
case class Repo(name: String, full_name: String)

object Repo {
  implicit val format: OFormat[Repo] = Json.format[Repo]
}
