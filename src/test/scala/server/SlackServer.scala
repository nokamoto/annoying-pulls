package server

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import play.api.mvc._
import play.api.routing.sird._
import slack.json.Message

class SlackServer(port: Int) {
  val incomingWebhook: String = s"http://localhost:$port/services/${UUID.randomUUID().toString}"

  val received: AtomicReference[Message] = new AtomicReference[Message]()

  val handler: PartialFunction[RequestHeader, Handler] = {
    case POST(p"/services/$id") => Action(BodyParsers.parse.json[Message]) { req =>
      received.set(req.body)
      Results.Ok("")
    }
  }
}
