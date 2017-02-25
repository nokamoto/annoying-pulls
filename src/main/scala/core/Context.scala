package core

import java.time.ZonedDateTime

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import core.Context.StaticContext
import play.api.libs.ws.ahc.AhcWSClient

class Context(val now: ZonedDateTime, val github: GithubSetting, val slack: SlackSetting) extends StaticContext {
  private[this] implicit val system = ActorSystem()

  private[this] implicit val materializer = ActorMaterializer()

  val ws: AhcWSClient = AhcWSClient()

  def shutdown(): Unit = {
    ws.close()
    system.terminate()
  }
}

object Context {
  private[this] val config = ConfigFactory.load()

  def github = GithubSetting(config.getConfig("github"))

  def slack = SlackSetting(config.getConfig("slack"))

  trait StaticContext {
    val now: ZonedDateTime

    val github: GithubSetting

    val slack: SlackSetting
  }

  def apply(): Context = new Context(now = ZonedDateTime.now(), github = github, slack = slack)
}
