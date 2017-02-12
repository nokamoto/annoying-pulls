package core

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext

class CoreContext(system: ActorSystem) {
  private[this] implicit val as = system

  private[this] implicit val m = ActorMaterializer()

  val ws: AhcWSClient = AhcWSClient()

  def shutdown(): Unit = {
    ws.close()
    system.terminate()
  }
}
