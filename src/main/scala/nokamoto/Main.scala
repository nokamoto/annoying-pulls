package nokamoto

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import core.CoreContext
import github.{GithubContext, GithubService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val gh = GithubContext(config.getConfig("github"))

    val core = new CoreContext(system = ActorSystem(), context = global)
    val ghService = new GithubService(gh = gh, core = core)

    val future = for {
      ns <- ghService.notifications
    } yield ()

    future.andThen {
      case Success(_) => println("done.")
      case Failure(e) => e.printStackTrace()
    }.andThen {
      case _ => core.shutdown()
    }

//    val future = for {
//      repos <- ws.url(s"$http/orgs/$org/repos").get().flatMap(_.json.validate[List[Repo]].future)
//      notifications <- Future.sequence(repos.map(forRepo(ws, _)))
//    } yield  {
//      val ordered = notifications.flatten.
//        sortBy(_.issue.createdAt.toEpochSecond).
//        filterNot(_.issue.labels.exists(label => excludeLabels.contains(label.name)))
//
//      val now = ZonedDateTime.now()
//
//      def daysAgo(d: ZonedDateTime): String = {
//        val days = ChronoUnit.DAYS.between(d, now)
//        s"$days days ago"
//      }
//
//      val text = ordered.map(n => s"<${n.pull.html_url}|[${daysAgo(n.issue.createdAt)}]${n.repo.name}:${n.pull.title}>")
//
//      text.mkString("\n")
//    }
//
//    future.flatMap { text =>
//      ws.url(slackWebhook).post(Json.obj("channel" -> slackChannel, "username" -> slackUsername, "icon_emoji" -> ":octocat:", "text" -> text))
//    }.map(res => println(res)).andThen { case _ => ws.close() }.andThen{ case _ => system.terminate() }
  }
}
