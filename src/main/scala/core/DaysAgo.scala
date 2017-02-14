package core

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import slack.AttachmentColor
import slack.AttachmentColor.{Danger, Good, Warning}

import scala.concurrent.duration._

trait DaysAgo {
  protected[this] def from: ZonedDateTime

  def daysAgo: FiniteDuration = ChronoUnit.DAYS.between(from, ZonedDateTime.now()).days

  def prettyDays: String = {
    val d = daysAgo.toDays
    val s = PrettyOps.s(d)
    s"$d day$s ago"
  }

  def color(warningAfter: FiniteDuration, dangerAfter: FiniteDuration): AttachmentColor = {
    daysAgo match {
      case ago if ago >= dangerAfter => Danger
      case ago if ago >= warningAfter => Warning
      case _ => Good
    }
  }
}
