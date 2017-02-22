package core

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import core.PullLike.AttachmentLike
import slack.AttachmentColor
import slack.AttachmentColor.{Danger, Good, Warning}
import slack.json.Attachment

import scala.concurrent.duration._

case class PullLike(fullName: String,
                    title: String,
                    htmlLink: String,
                    number: Long,
                    createdAt: ZonedDateTime,
                    login: String,
                    avatarUrl: String) {

  private[this] def ago(unit: ChronoUnit, now: ZonedDateTime): Long = unit.between(createdAt, now)

  private[this] def pretty(t: => Long, unit: String, f: => String): String = {
    if (t > 0) s"$t $unit${PrettyOps.s(t)} ago" else f
  }

  private[this] def prettyDays(now: ZonedDateTime): String = {
    def seconds: String = pretty(ago(ChronoUnit.SECONDS, now), "second", "0 second ago")
    def minutes: String = pretty(ago(ChronoUnit.MINUTES, now), "minute", seconds)
    def hours: String = pretty(ago(ChronoUnit.HOURS, now), "hour", minutes)

    pretty(ago(ChronoUnit.DAYS, now), "day", hours)
  }

  private[this] def color(now: ZonedDateTime, warningAfter: FiniteDuration, dangerAfter: FiniteDuration): AttachmentColor = {
    ago(ChronoUnit.DAYS, now).days match {
      case ago if ago >= dangerAfter => Danger
      case ago if ago >= warningAfter => Warning
      case _ => Good
    }
  }

  def attachment(now: ZonedDateTime) = AttachmentLike(
    title = s"[$fullName] $title #$number",
    titleLink = htmlLink,
    footer = s"$login opened ${prettyDays(now)}",
    footerIcon = avatarUrl,
    color = color(now, _, _))
}

object PullLike {
  case class AttachmentLike(title: String,
                            titleLink: String,
                            footer: String,
                            footerIcon: String,
                            color: (FiniteDuration, FiniteDuration) => AttachmentColor) {
    def make(warningAfter: FiniteDuration, dangerAfter: FiniteDuration): Attachment = {
      Attachment(
        title = title,
        title_link = titleLink,
        footer = footer,
        color = color(warningAfter, dangerAfter),
        footer_icon = footerIcon)
    }
  }
}
