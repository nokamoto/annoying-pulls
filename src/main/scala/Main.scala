import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import core.{CoreContext, IncomingWebhookService}
import github.{GithubService, GithubSetting}
import slack.{SlackService, SlackSetting}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main {
  def run(core: CoreContext, gh: GithubSetting, sl: SlackSetting): Future[Unit] = {
    val ghService = new GithubService(gh = gh, core = core)
    val iwService = new IncomingWebhookService(core = core, gh = gh, sl = sl)
    val slService = new SlackService(sl = sl, core = core, service = iwService)

    val future = for {
      pulls <- ghService.pulls()
      _ <- slService.webhook(pulls)
    } yield ()

    future.andThen {
      case Success(_) => println("done.")
      case Failure(e) => e.printStackTrace()
    }
  }

  def main(args: Array[String]): Unit = {
    val core = new CoreContext(system = ActorSystem())
    val config = ConfigFactory.load()
    val gh = GithubSetting(config.getConfig("github"))
    val sl = SlackSetting(config.getConfig("slack"))

    run(core, gh, sl).andThen { case _ => core.shutdown() }
  }
}
