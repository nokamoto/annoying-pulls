package server

import java.time.ZonedDateTime

import core.PullLike
import slack.json.Attachment

import scala.concurrent.duration._

case class GithubPull(fullName: String, number: Int, title: String, url: String, createdAt: ZonedDateTime, labels: List[String]) {
  val attachment: Attachment = {
    PullLike(fullName = fullName, title = title, htmlLink = url, number = number, createdAt = createdAt).
      attachment.make(warningAfter = GithubPull.warningAfter, dangerAfter = GithubPull.dangerAfter)
  }

  def labeled(label: String): GithubPull = copy(labels = label :: labels)
}

object GithubPull {
  val dangerAfter: FiniteDuration = 14.days

  val warningAfter: FiniteDuration = 7.days
}
