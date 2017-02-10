package github

import core.{CoreContext, Notification}
import github.json.{Issue, Pull, Repo}
import play.api.libs.json._

import scala.concurrent.Future

class GithubService(gh: GithubContext, core: CoreContext) {
  import core._

  private[this] def get[A : Reads](path: String): Future[A] = {
    val url = s"${gh.api}$path"

    println(s"GET $url")

    ws.url(url).get().map(res => (res, res.json.validate[A])).flatMap {
      case (_, JsSuccess(value, _)) => Future.successful(value)
      case (res, JsError(errors)) =>
        println(s"${res.status} - ${res.json}")
        Future.failed(JsResultException(errors))
    }
  }

  private[this] def getRepos(ownerOpt: Option[Owner], path: Owner => String): Future[List[(Repo, Owner)]] = {
    ownerOpt match {
      case Some(owner) => get[List[Repo]](path(owner)).map(_.map(repo => (repo, owner)))
      case None => Future.successful(Nil)
    }
  }

  private[this] def getOrgRepos: Future[List[(Repo, Owner)]] = getRepos(gh.org, org => s"/orgs/${org.name}/repos")

  private[this] def getUserRepos: Future[List[(Repo, Owner)]] = {
    getRepos(gh.username, username => s"/users/${username.name}/repos")
  }

  private[this] def getPulls(repo: Repo, owner: Owner): Future[List[(Repo, Pull)]] = {
    get[List[Pull]](s"/repos/${owner.name}/${repo.name}/pulls").
      map(_.map(pull => (repo, pull)))
  }

  private[this] def getIssue(repo: Repo, pull: Pull): Future[(Repo, Pull, Issue)] = {
    get[Issue](pull.issue_url).map(issue => (repo, pull, issue))
  }

  /**
    * List all pull requests filtered by the excluded labels for the organization repositories.
    */
  def notifications: Future[List[Notification]] = {
    for {
      orgs <- getOrgRepos
      users <- getUserRepos
      pulls <- Future.sequence((orgs ++ users).map { case (repo, owner) => getPulls(repo, owner) })
      issues <- Future.sequence(pulls.flatten.map { case (repo, pull) => getIssue(repo, pull) })
    } yield {
      issues.
        map { case (repo, pull, issue) => Notification(repo = repo, pull = pull, issue = issue) }.
        filterNot(_.issue.labels.exists(label => gh.excludedLabels.contains(label.name)))
    }
  }
}
