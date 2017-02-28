package github

import java.time.{Instant, ZoneId, ZonedDateTime}

import core.{Context, PullRequest}
import github.json.{Issue, Pull, Pulls, Repo}
import play.api.libs.json._
import play.api.libs.ws.WSResponse

import scala.concurrent.{ExecutionContext, Future}

class GithubService(context: Context)(implicit ec: ExecutionContext) {
  import context._

  private[this] def header(h: String, res: WSResponse): String = s"""$h: ${res.header(h).getOrElse("")}"""

  private[this] val LIMIT = "X-RateLimit-Limit"

  private[this] val REMAINING = "X-RateLimit-Remaining"

  private[this] val RESET = "X-RateLimit-Reset"

  private[this] def get[A : Reads](url: String): Future[A] = {
    println(s"GET $url")

    ws.url(url).get().map { (res: WSResponse) =>
      val time = ZonedDateTime.ofInstant(
        Instant.ofEpochSecond(res.header(RESET).map(_.toLong).getOrElse(0)),
        ZoneId.systemDefault())

      println(header(LIMIT, res))
      println(header(REMAINING, res))
      println(header(RESET, res) + s"($time)")
      println(s"${res.status} - ${res.json}")

      res.json.validate[A]
    }.flatMap {
      case JsSuccess(value, _) => Future.successful(value)
      case JsError(errors) => Future.failed(JsResultException(errors))
    }
  }

  private[this] def getRepos(ownerOpt: Option[Owner], path: Owner => String): Future[List[(Owner, Repo)]] = {
    ownerOpt match {
      case Some(owner) => get[List[Repo]](path(owner)).map(_.map((owner, _)))
      case None => Future.successful(Nil)
    }
  }

  private[this] def getOrgRepos = getRepos(github.org, org => s"${github.api}/orgs/${org.name}/repos")

  private[this] def getUserRepos = getRepos(github.username, username => s"${github.api}/users/${username.name}/repos")

  private[this] def getPullRequest(repo: Repo, pulls: Pulls): Future[PullRequest] = {
    for {
      issue <- get[Issue](pulls.issue_url)
      pull <- get[Pull](pulls.url)
    } yield PullRequest(repo = repo, pulls = pulls, pull = pull, issue = issue)
  }

  private[this] def getPullRequests(owner: Owner, repo: Repo): Future[List[PullRequest]] = {
    for {
      pulls <- get[List[Pulls]](s"${github.api}/repos/${owner.name}/${repo.name}/pulls")
      res <- Future.sequence(pulls.map(getPullRequest(repo, _)))
    } yield res
  }

  /**
    * List all pull requests for the organization and user repositories.
    */
  def pulls(): Future[List[PullRequest]] = {
    for {
      org <- getOrgRepos
      user <- getUserRepos
      res <- Future.sequence((org ++ user).map((getPullRequests _).tupled))
    } yield res.flatten
  }
}
