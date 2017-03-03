package github

import java.util.concurrent.atomic.AtomicInteger

import core.{Context, Logger, PullRequest}
import github.json.{Issue, Pull, Pulls, Repo}
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

class GithubService(context: Context)(implicit ec: ExecutionContext) extends Logger {
  import context._

  private[this] val counter = new AtomicInteger(0)

  private[this] val AUTHORIZATION = "Authorization"

  private[this] def get[A : Reads](url: String): Future[A] = {
    val n = counter.incrementAndGet()

    logger.info(s"[$n] GET $url")

    github.personalAccessToken.foldLeft(ws.url(url)) { case (req, token) =>
      req.withHeaders(AUTHORIZATION -> s"token $token")
    }.get().map { res =>
      val rateLimit = RateLimit(res)

      logger.info(s"[$n] ${res.status} - ${rateLimit.pretty}")
      logger.info(s"[$n] ${res.json}")

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

  private[this] def sequentially[A, B](as: List[A], f: A => Future[B]): Future[List[B]] = {
    as.foldLeft(Future.successful(List.empty[B])) { case (fbs, a) =>
      for {
        bs <- fbs
        b <- f(a)
      } yield b :: bs
    }.map(_.reverse)
  }

  private[this] def getPullRequests(owner: Owner, repo: Repo): Future[List[PullRequest]] = {
    for {
      pulls <- get[List[Pulls]](s"${github.api}/repos/${owner.name}/${repo.name}/pulls")
      res <- sequentially(pulls, (getPullRequest _).curried(repo))
    } yield res
  }

  /**
    * List all pull requests for the organization and user repositories.
    */
  def pulls(): Future[List[PullRequest]] = for {
    org <- getOrgRepos
    user <- getUserRepos
    res <- sequentially(org ++ user, (getPullRequests _).tupled)
  } yield res.flatten
}
