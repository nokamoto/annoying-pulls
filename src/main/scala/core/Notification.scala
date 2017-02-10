package core

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import github.json.{Issue, Pull, Repo}

case class Notification(repo: Repo, pull: Pull, issue: Issue) {
  val createdAt: ZonedDateTime = issue.createdAt

  val daysAgo: Long = ChronoUnit.DAYS.between(createdAt, ZonedDateTime.now())
}
