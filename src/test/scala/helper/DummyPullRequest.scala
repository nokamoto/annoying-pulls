package helper

import java.time.ZonedDateTime

import core.PullRequest
import github.json.{Issue, Pull, Repo, User}

trait DummyPullRequest {
  def pullRequest(createdAt: ZonedDateTime, title: String = ":title", number: Long = 1, comments: Long = 0): PullRequest = {
    val user = User(login = ":login", avatar_url = "https://avatars.githubusercontent.com/u/4374383?v=3")

    val repo = Repo(name = ":repo", full_name = ":owner/:repo")

    val pull = Pull(
      html_url = "https://localhost/:owner/:repo:/pulls/:number",
      title = s"$title",
      issue_url = "https://localhost/:owner/:repo/issues/:number",
      number = number)

    val issue = Issue(labels = Nil, created_at = createdAt.toString, user = user, comments = comments)

    PullRequest(repo = repo, pull = pull, issue = issue)
  }
}
