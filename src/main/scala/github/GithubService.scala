package github

import java.time.{Instant, ZoneId, ZonedDateTime}

import core.{CoreContext, PullRequest}
import github.json.{Issue, Pull, Repo}
import play.api.libs.json._
import play.api.libs.ws.WSResponse

import scala.concurrent.{ExecutionContext, Future}

class GithubService(gh: GithubSetting, core: CoreContext)(implicit ec: ExecutionContext) {
  import core._

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

  private[this] def getRepos(ownerOpt: Option[Owner], path: Owner => String): Future[List[(Repo, Owner)]] = {
    ownerOpt match {
      case Some(owner) => get[List[Repo]](path(owner)).map(_.map(repo => (repo, owner)))
      case None => Future.successful(Nil)
    }
  }

  private[this] def getOrgRepos: Future[List[(Repo, Owner)]] = getRepos(gh.org, org => s"${gh.api}/orgs/${org.name}/repos")

  private[this] def getUserRepos: Future[List[(Repo, Owner)]] = {
    getRepos(gh.username, username => s"${gh.api}/users/${username.name}/repos")
  }

  private[this] def getPulls(repo: Repo, owner: Owner): Future[List[(Repo, Pull)]] = {
    get[List[Pull]](s"${gh.api}/repos/${owner.name}/${repo.name}/pulls").
      map(_.map(pull => (repo, pull)))
  }

  private[this] def getIssue(repo: Repo, pull: Pull): Future[(Repo, Pull, Issue)] = {
    get[Issue](pull.issue_url).map(issue => (repo, pull, issue))
  }

  /**
    * List all pull requests filtered by the excluded labels for the organization and user repositories.
    */
  def pulls(): Future[List[PullRequest]] = {
    for {
      org <- getOrgRepos
      user <- getUserRepos
      pulls <- Future.sequence((org ++ user).map { case (repo, owner) => getPulls(repo, owner) })
      issues <- Future.sequence(pulls.flatten.map { case (repo, pull) => getIssue(repo, pull) })
    } yield {
      issues.
        map { case (repo, pull, issue) => PullRequest(repo = repo, pull = pull, issue = issue) }.
        filterNot(_.issue.labels.exists(label => gh.excludedLabels.contains(label.name)))
    }
  }
}
