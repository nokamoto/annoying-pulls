package core

import com.typesafe.config.Config
import core.ConfigOps.Implicits.Ops
import github.Owner

import scala.collection.JavaConverters._

/**
  * @param api e.g. https://api.github.com.
  * @param org :org, see https://developer.github.com/v3/repos/#list-organization-repositories.
  * @param username :username, see https://developer.github.com/v3/repos/#list-user-repositories.
  * @param excludedLabels filter a pull request if it contains one of the excluded labels.
  * @param personalAccessToken see https://github.com/blog/1509-personal-api-tokens.
  */
case class GithubSetting(api: String,
                         org: Option[Owner],
                         username: Option[Owner],
                         excludedLabels: List[String],
                         personalAccessToken: Option[String])

object GithubSetting {
  def apply(config: Config): GithubSetting = {
    GithubSetting(
      api = config.getString("api"),
      org = config.getOptionString("org").map(Owner),
      username = config.getOptionString("username").map(Owner),
      excludedLabels = config.getStringList("excluded-labels").asScala.toList,
      personalAccessToken = config.getOptionString("personal-access-token"))
  }
}
