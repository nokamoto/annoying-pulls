package core

import github.json.{Issue, Pull, Repo}

case class PullRequest(repo: Repo, pull: Pull, issue: Issue) {
  val createdAt = issue.createdAt

  val like = PullLike(
    fullName = repo.full_name,
    title = pull.title,
    htmlLink = pull.html_url,
    number = pull.number,
    createdAt = createdAt)
}
