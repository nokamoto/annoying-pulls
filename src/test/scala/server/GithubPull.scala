package server

import java.time.ZonedDateTime

import core.DaysAgo
import slack.json.Attachment

case class GithubPull(number: Int, title: String, url: String, createdAt: ZonedDateTime, labels: List[String])
  extends DaysAgo {

  override protected[this] def from: ZonedDateTime = createdAt

  def footer: String = prettyDays

  def attachment(owner: String, repo: String): Attachment = {
    Attachment(
      title = s"[$owner/$repo] $title #$number",
      title_link = url,
      footer = footer,
      color = "good")
  }
}
