package server

import java.time.ZonedDateTime

import core.{AttachmentTitle, DaysAgo}
import slack.json.Attachment

case class GithubPull(fullName: String, number: Int, title: String, url: String, createdAt: ZonedDateTime, labels: List[String])
  extends DaysAgo with AttachmentTitle {

  override protected[this] def from: ZonedDateTime = createdAt

  override protected[this] def repoFullName: String = fullName

  override protected[this] def pullTitle: String = title

  override protected[this] def hashNumber: Long = number

  def footer: String = prettyDays

  def attachment(owner: String, repo: String): Attachment = {
    Attachment(
      title = attachmentTitle,
      title_link = url,
      footer = footer,
      color = "good")
  }
}
