import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import core.CoreContext
import github.{GithubService, GithubSetting}
import slack.{SlackService, SlackSetting}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val gh = GithubSetting(config.getConfig("github"))
    val sl = SlackSetting(config.getConfig("slack"))

    val core = new CoreContext(system = ActorSystem(), context = global)
    val ghService = new GithubService(gh = gh, core = core)
    val slService = new SlackService(sl = sl, core = core)

    val future = for {
      pulls <- ghService.pulls()
      _ <- slService.webhook(pulls)
    } yield ()

    future.andThen {
      case Success(_) => println("done.")
      case Failure(e) => e.printStackTrace()
    }.andThen {
      case _ => core.shutdown()
    }
  }
}
