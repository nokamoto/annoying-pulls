package server

import java.time.ZonedDateTime

import core.Context.StaticContext
import core.PullLike
import slack.json.Attachment

case class GithubPull(fullName: String,
                      number: Int,
                      title: String,
                      url: String,
                      createdAt: ZonedDateTime,
                      labels: List[String],
                      login: String,
                      avatarUrl: String,
                      comments: Long,
                      reviewComments: Long) {

  private[this] val like = {
    PullLike(fullName = fullName,
             title = title,
             htmlLink = url,
             number = number,
             createdAt = createdAt,
             login = login,
             avatarUrl = avatarUrl,
             comments = comments + reviewComments)
  }

  def attachment(context: StaticContext): Attachment = like.attachment(context)

  def labeled(label: String): GithubPull = copy(labels = label :: labels)
}
