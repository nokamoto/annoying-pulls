package github

import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * @param api e.g. https://api.github.com.
  * @param org :org, see https://developer.github.com/v3/repos/#list-organization-repositories.
  * @param username :username, see https://developer.github.com/v3/repos/#list-user-repositories.
  * @param excludedLabels filter a pull request if it contains one of the excluded labels.
  */
case class GithubContext(api: String, org: Option[Owner], username: Option[Owner], excludedLabels: List[String])

object GithubContext {
  def apply(config: Config): GithubContext = {
    GithubContext(
      api = config.getString("api"),
      org = Try(config.getString("org")).toOption.map(Owner),
      username = Try(config.getString("username")).toOption.map(Owner),
      excludedLabels = config.getStringList("excluded-labels").asScala.toList)
  }
}
