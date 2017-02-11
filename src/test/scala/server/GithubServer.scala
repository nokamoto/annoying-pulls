package server

import java.time.ZonedDateTime
import java.util.UUID

import github.json.{Issue, Pull, Repo}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Handler, RequestHeader, _}
import play.api.routing.sird._

/**
  * Mocking github api server.
  *
  * @see [[https://developer.github.com/v3/repos/#list-organization-repositories]]
  * @see [[https://developer.github.com/v3/repos/#list-user-repositories]]
  * @see [[https://developer.github.com/v3/pulls/#list-pull-requests]]
  * @see [[https://developer.github.com/v3/issues/#get-a-single-issue]]
  */
class GithubServer(port: Int) {
  private[this] def ok[A : Writes](value: A) = Action(Results.Ok(Json.toJson(value)))

  private[this] def repos(owner: String): List[Repo] = {
    List.fill(3)(UUID.randomUUID().toString).map(name => Repo(name = name, full_name = s"$owner/$name"))
  }

  private[this] def htmlUrl(owner: String, repo: String, n: Int): String = s"http://localhost:$port/$owner/$repo/pulls/$n"

  private[this] def issueUrl(owner: String, repo: String, n: Int): String = s"http://localhost:$port/repos/$owner/$repo/issues/$n"

  private[this] def title(n: Int): String = s"#$n"

  private[this] def pulls(owner: String, repo: String): List[Pull] = {
    List(1, 10, 100).map(n =>
      Pull(html_url = htmlUrl(owner, repo, n), title = title(n), issue_url = issueUrl(owner, repo, n), number = n))
  }

  private[this] def issue(owner: String, repo: String, number: Int): Issue = {
    Issue(labels = Nil, created_at = ZonedDateTime.now().toString)
  }

  val handler: PartialFunction[RequestHeader, Handler] = {
    case GET(p"/orgs/$org/repos") => ok(repos(org))

    case GET(p"/users/$user/repos") => ok(repos(user))

    case GET(p"/repos/$owner/$repo/pulls") => ok(pulls(owner, repo))

    case GET(p"/repos/$owner/$repo/issues/$number") => ok(issue(owner, repo, number.toInt))
  }

  val api = s"http://localhost:$port"
}
