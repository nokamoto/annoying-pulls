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

  private[this] def daysAgo(now: ZonedDateTime): FiniteDuration = ChronoUnit.DAYS.between(createdAt, now).days

  private[this] def prettyDays(now: ZonedDateTime): String = {
    val d = daysAgo(now).toDays
    val s = PrettyOps.s(d)
    s"$d day$s ago"
  }

  private[this] def color(now: ZonedDateTime, warningAfter: FiniteDuration, dangerAfter: FiniteDuration): AttachmentColor = {
    daysAgo(now) match {
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
