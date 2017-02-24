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
                      login: String,
                      avatarUrl: String,
                      comments: Long) {

  private[this] val like = {
    PullLike(
      fullName = fullName,
      title = title,
      htmlLink = url,
      number = number,
      createdAt = createdAt,
      login = login,
      avatarUrl = avatarUrl,
      comments = comments)
  }

  def attachment(context: Context): Attachment = like.attachment(context)

  def labeled(label: String): GithubPull = copy(labels = label :: labels)
}
