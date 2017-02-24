package server

import java.time.ZonedDateTime

import core.{Context, PullLike}
import slack.json.Attachment

case class GithubPull(fullName: String,
                      number: Int,
                      title: String,
                      url: String,
                      createdAt: ZonedDateTime,
                      labels: List[String],
                      login: String, avatarUrl: String) {

  private[this] val like = {
    PullLike(
      fullName = fullName,
      title = title,
      htmlLink = url,
      number = number,
      createdAt = createdAt,
      login = login,
      avatarUrl = avatarUrl)
  }

  def attachment(context: Context): Attachment = {
    like.attachment(context.now).make(warningAfter = context.slack.warningAfter, dangerAfter = context.slack.dangerAfter)
  }

  def labeled(label: String): GithubPull = copy(labels = label :: labels)
}
