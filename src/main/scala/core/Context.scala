package core

import java.time.ZonedDateTime

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import play.api.libs.ws.ahc.AhcWSClient

class Context(system: ActorSystem, val now: ZonedDateTime, val github: GithubSetting, val slack: SlackSetting) {
  private[this] implicit val as = system

  private[this] implicit val m = ActorMaterializer()

  val ws: AhcWSClient = AhcWSClient()

  def shutdown(): Unit = {
    ws.close()
    system.terminate()
  }
}

object Context {
  def apply(): Context = {
    val config = ConfigFactory.load()
    new Context(
      system = ActorSystem(),
      now = ZonedDateTime.now(),
      github = GithubSetting(config.getConfig("github")),
      slack = SlackSetting(config.getConfig("slack")))
  }
}
