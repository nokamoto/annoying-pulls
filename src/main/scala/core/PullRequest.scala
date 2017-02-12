package core

import java.time.ZonedDateTime

import github.json.{Issue, Pull, Repo}

case class PullRequest(repo: Repo, pull: Pull, issue: Issue) extends DaysAgo {
  val createdAt: ZonedDateTime = issue.createdAt

  override protected[this] def from: ZonedDateTime = createdAt
}
