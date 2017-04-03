package helper

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.ExecutionContext

trait DefaultFutures extends ScalaFutures {
  protected[this] implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  protected[this] implicit val global: ExecutionContext =
    ExecutionContext.global
}
