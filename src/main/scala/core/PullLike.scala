package core

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import core.PullLike.AttachmentLike
import slack.AttachmentColor
import slack.AttachmentColor.{Danger, Good, Warning}
import slack.json.Attachment

import scala.concurrent.duration._

case class PullLike(fullName: String, title: String, htmlLink: String, number: Long, createdAt: ZonedDateTime) {
  private[this] def daysAgo: FiniteDuration = ChronoUnit.DAYS.between(createdAt, ZonedDateTime.now()).days

  private[this] def prettyDays: String = {
    val d = daysAgo.toDays
    val s = PrettyOps.s(d)
    s"$d day$s ago"
  }

  private[this] def color(warningAfter: FiniteDuration, dangerAfter: FiniteDuration): AttachmentColor = {
    daysAgo match {
      case ago if ago >= dangerAfter => Danger
      case ago if ago >= warningAfter => Warning
      case _ => Good
    }
  }

  val attachment = AttachmentLike(
    title = s"[$fullName] $title #$number",
    titleLink = htmlLink,
    footer = prettyDays,
    color = color)
}

object PullLike {
  case class AttachmentLike(title: String,
                            titleLink: String,
                            footer: String,
                            color: (FiniteDuration, FiniteDuration) => AttachmentColor) {
    def make(warningAfter: FiniteDuration, dangerAfter: FiniteDuration): Attachment = {
      Attachment(title = title, title_link = titleLink, footer = footer, color = color(warningAfter, dangerAfter))
    }
  }
}
