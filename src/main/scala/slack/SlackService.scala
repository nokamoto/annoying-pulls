package slack

import core.{Context, IncomingWebhookService, PullRequest}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

class SlackService(context: Context)(implicit ec: ExecutionContext) {
  import context._

  private[this] val service = new IncomingWebhookService(context)

  /**
    * post the pull requests attachments to the slack incoming webhook.
    */
  def webhook(pulls: List[PullRequest]): Future[Unit] = {
    println(slack.incomingWebhook)

    ws.url(slack.incomingWebhook).post(Json.toJson(service.incomingWebhook(pulls))).flatMap {
      case res if res.status / 100 == 2 =>
        println(s"${res.status} - ${res.body}")
        Future.successful(())

      case res =>
        Future.failed(new RuntimeException(s"${res.status} - ${res.body}"))
    }
  }
}
