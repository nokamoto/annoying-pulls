import github.GithubSetting
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import server.MockServers
import slack.SlackSetting

import scala.concurrent.duration._

class MainSpec extends FlatSpec with ScalaFutures {
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  it should "post no pull requests to incoming webhook if :org and :username are not supplied" in {
    val servers = new MockServers(9000)
    try {
      val gh = GithubSetting(
        api = servers.github.api,
        org = None,
        username = None,
        excludedLabels = Nil)

      val sl = SlackSetting(
        incomingWebhook = servers.slack.incomingWebhook,
        channel = None,
        username = None,
        iconEmoji = None,
        warningAfter = 7.days,
        dangerAfter = 14.days,
        attachmentsLimit = 20)

      whenReady(Main.run(gh, sl)) { _ =>
        val res = servers.slack.recevied.get()
        assert(res.text === "0 pull request opened")
        assert(res.attachments === Nil)
      }

      println("done")
    } finally {
      servers.shutdown()
    }
  }
}
