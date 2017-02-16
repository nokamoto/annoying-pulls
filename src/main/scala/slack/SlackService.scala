package slack

import core.{CoreContext, IncomingWebhookService, PullRequest}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

class SlackService(sl: SlackSetting, core: CoreContext, service: IncomingWebhookService)(implicit ec: ExecutionContext) {
  import core._

  /**
    * post the pull requests attachments to the slack incoming webhook.
    */
  def webhook(pulls: List[PullRequest]): Future[Unit] = {
    println(sl.incomingWebhook)

    ws.url(sl.incomingWebhook).post(Json.toJson(service.incomingWebhook(pulls))).flatMap {
      case res if res.status / 100 == 2 =>
        println(s"${res.status} - ${res.body}")
        Future.successful(())

      case res =>
        Future.failed(new RuntimeException(s"${res.status} - ${res.body}"))
    }
  }
}
