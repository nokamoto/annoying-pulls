package helper

import java.time.ZonedDateTime

import core.PullRequest
import github.json._

trait DummyPullRequest {
  def pullRequest(createdAt: ZonedDateTime,
                  title: String = ":title",
                  number: Long = 1,
                  comments: Long = 0,
                  reviewComments: Long = 0): PullRequest = {

    val user = User(login = ":login", avatar_url = "https://avatars.githubusercontent.com/u/4374383?v=3")

    val repo = Repo(name = ":repo", full_name = ":owner/:repo")

    val pulls = Pulls(
      url = "https://localhost/:owner/:repo:/pulls/:number",
      html_url = "https://localhost/:owner/:repo:/pulls/:number",
      title = s"$title",
      issue_url = "https://localhost/:owner/:repo/issues/:number",
      number = number)

    val pull = Pull(comments = comments, review_comments = reviewComments)

    val issue = Issue(labels = Nil, created_at = createdAt.toString, user = user)

    PullRequest(repo = repo, pulls = pulls, pull = pull, issue = issue)
  }
}
