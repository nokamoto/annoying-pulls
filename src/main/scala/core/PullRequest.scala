package core

import java.time.ZonedDateTime

import github.json.{Issue, Pull, Pulls, Repo}

case class PullRequest(repo: Repo, pulls: Pulls, pull: Pull, issue: Issue) {
  val createdAt: ZonedDateTime = issue.createdAt

  val like = PullLike(
    fullName = repo.full_name,
    title = pulls.title,
    htmlLink = pulls.html_url,
    number = pulls.number,
    createdAt = createdAt,
    login = issue.user.login,
    avatarUrl = issue.user.avatar_url,
    comments = pull.comments + pull.review_comments
  )
}
