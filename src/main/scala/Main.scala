import core.Context
import github.GithubService
import slack.SlackService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main {
  def run(context: Context): Future[Unit] = {
    val github = new GithubService(context)
    val slack = new SlackService(context)

    val future = for {
      pulls <- github.pulls()
      _ <- slack.webhook(pulls)
    } yield ()

    future.andThen {
      case Success(_) => println("done.")
      case Failure(e) => e.printStackTrace()
    }
  }

  def main(args: Array[String]): Unit = {
    val context = Context()
    run(context).andThen { case _ => context.shutdown() }
  }
}
