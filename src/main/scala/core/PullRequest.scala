package core

import java.time.ZonedDateTime

import github.json.{Issue, Pull, Repo}

case class PullRequest(repo: Repo, pull: Pull, issue: Issue) extends DaysAgo with AttachmentTitle {
  val createdAt: ZonedDateTime = issue.createdAt

  override protected[this] def from: ZonedDateTime = createdAt

  override protected[this] def repoFullName: String = repo.full_name

  override protected[this] def pullTitle: String = pull.title

  override protected[this] def hashNumber: Long = pull.number
}
