package core

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.util.Try

object ConfigOps {
  object Implicits {
    implicit class Ops(config: Config) {
      def getOptionString(path: String): Option[String] =
        Try(config.getString(path)).toOption

      def getFiniteDuration(path: String): FiniteDuration =
        config.getDuration(path, TimeUnit.SECONDS).seconds
    }
  }
}
