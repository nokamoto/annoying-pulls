package server

import java.util.concurrent.atomic.AtomicReference

import github.json._
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
class GithubServer(port: Int, pageSize: Int) {
  val org = new AtomicReference[GithubOrg]()

  val user = new AtomicReference[GithubUser]()

  private[this] def ok[A : Writes](value: A, headers: (String, String)*) = Action(Results.Ok(Json.toJson(value)).withHeaders(headers: _*))

  private[this] def getOwner(owner: String): GithubOwner = if (owner == org.get().owner) org.get() else user.get()

  private[this] def repos(owner: String, page: Int): List[Repo] = {
    getOwner(owner).repos.splitAt(page * pageSize)._2.splitAt(pageSize)._1.take(pageSize).map(repo => Repo(name = repo.name, full_name = repo.fullName))
  }

  private[this] def reposLink(typ: String, owner: String, page: Int): List[(String, String)] = {
    if (getOwner(owner).repos.splitAt((page + 1) * pageSize)._2.nonEmpty) {
      "Link" -> s"""<http://localhost:$port/$typ/$owner/repos/${page + 1}>; rel="next", <http://localhost:$port>; rel="dummy"""" :: Nil
    } else {
      Nil
    }
  }

  private[this] def issueUrl(owner: String, repo: String, n: Int): String = s"http://localhost:$port/repos/$owner/$repo/issues/$n"

  private[this] def pullUrl(owner: String, repo: String, n: Int): String = s"http://localhost:$port/repos/$owner/$repo/pulls/$n"

  private[this] def pulls(owner: String, repo: String): List[Pulls] = {
    getOwner(owner).repos.find(_.name == repo).get.pulls.map { pull =>
      Pulls(
        url = pullUrl(owner, repo, pull.number),
        html_url = pull.url,
        title = pull.title,
        issue_url = issueUrl(owner, repo, pull.number),
        number = pull.number)
    }
  }

  private[this] def issue(owner: String, repo: String, number: Int): Issue = {
    val res = for {
      repo <- getOwner(owner).repos.find(_.name == repo)
      pull <- repo.pulls.find(_.number == number)
    } yield {
      Issue(
        labels = pull.labels.map(Label.apply),
        created_at = pull.createdAt.toString,
        user = User(login = pull.login, avatar_url = pull.avatarUrl))
    }

    res.get
  }

  private[this] def pull(owner: String, repo: String, number: Int): Pull = {
    val res = for {
      repo <- getOwner(owner).repos.find(_.name == repo)
      pull <- repo.pulls.find(_.number == number)
    } yield {
      Pull(comments = pull.comments, review_comments = pull.reviewComments)
    }

    res.get
  }

  val handler: PartialFunction[RequestHeader, Handler] = {
    case GET(p"/orgs/$org/repos") => ok(repos(org, 0), reposLink("orgs", org, 0): _*)

    case GET(p"/orgs/$org/repos/$page") => ok(repos(org, page.toInt), reposLink("orgs", org, page.toInt): _*)

    case GET(p"/users/$user/repos") => ok(repos(user, 0), reposLink("users", user, 0): _*)

    case GET(p"/users/$user/repos/$page") => ok(repos(user, page.toInt), reposLink("users", user, page.toInt): _*)

    case GET(p"/repos/$owner/$repo/pulls") => ok(pulls(owner, repo))

    case GET(p"/repos/$owner/$repo/issues/$number") => ok(issue(owner, repo, number.toInt))

    case GET(p"/repos/$owner/$repo/pulls/$number") => ok(pull(owner, repo, number.toInt))
  }

  val api = s"http://localhost:$port"
}
