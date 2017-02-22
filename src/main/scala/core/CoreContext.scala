package core

import java.time.ZonedDateTime

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.ahc.AhcWSClient

class CoreContext(system: ActorSystem) {
  private[this] implicit val as = system

  private[this] implicit val m = ActorMaterializer()

  val now: ZonedDateTime = ZonedDateTime.now()

  val ws: AhcWSClient = AhcWSClient()

  def shutdown(): Unit = {
    ws.close()
    system.terminate()
  }
}
