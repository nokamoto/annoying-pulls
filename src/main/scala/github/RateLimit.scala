package github

import java.time.{Instant, ZoneId, ZonedDateTime}

import play.api.libs.ws.WSResponse

import github.RateLimit._

/**
  * @see [[https://developer.github.com/v3/#rate-limiting]]
  */
case class RateLimit(limit: Option[Long],
                     remaining: Option[Long],
                     reset: Option[Long]) {
  val resetAt: Option[ZonedDateTime] = {
    reset.map(
      epoch =>
        ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch),
                                ZoneId.systemDefault()))
  }

  val pretty: String = {
    val l = limit.map(_.toString).getOrElse("")
    val rem = remaining.map(_.toString).getOrElse("")
    val res = reset.map(_.toString).getOrElse("") + resetAt
      .map(at => s" ($at)")
      .getOrElse("")
    s"""$LIMIT: $l, $REMAINING: $rem, $RESET: $res"""
  }
}

object RateLimit {
  private val LIMIT = "X-RateLimit-Limit"

  private val REMAINING = "X-RateLimit-Remaining"

  private val RESET = "X-RateLimit-Reset"

  def apply(res: WSResponse): RateLimit = {
    def integer(key: String): Option[Long] = res.header(key).map(_.toLong)

    RateLimit(limit = integer(LIMIT),
              remaining = integer(REMAINING),
              reset = integer(RESET))
  }
}
