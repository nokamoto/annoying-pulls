package nokamoto

import core.{Context, Logger}
import github.GithubService
import slack.SlackService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main extends Logger {
  def run(context: Context)(implicit ec: ExecutionContext): Future[Unit] = {
    val github = new GithubService(context)
    val slack = new SlackService(context)

    val future = for {
      pulls <- github.pulls()
      _ <- slack.webhook(pulls)
    } yield ()

    future.andThen {
      case Success(_) => logger.info("done.")
      case Failure(e) => logger.info("failed.", e)
    }
  }

  def main(args: Array[String]): Unit = {
    implicit val global = ExecutionContext.global
    val context = Context()
    run(context).andThen { case _ => context.shutdown() }
  }
}
