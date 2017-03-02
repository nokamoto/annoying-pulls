package server

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import play.api.mvc._
import play.api.routing.sird._
import slack.json.IncomingWebhook

class SlackServer(port: Int) {
  val incomingWebhook: String = s"http://localhost:$port/services/${UUID.randomUUID().toString}"

  val received: AtomicReference[IncomingWebhook] = new AtomicReference[IncomingWebhook]()

  val handler: PartialFunction[RequestHeader, Handler] = {
    case POST(p"/services/$id") => Action(BodyParsers.parse.json[IncomingWebhook]) { req =>
      received.set(req.body)
      Results.Ok(id)
    }
  }
}
