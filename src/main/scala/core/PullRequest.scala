package core

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import github.json.{Issue, Pull, Repo}

import scala.concurrent.duration._

case class PullRequest(repo: Repo, pull: Pull, issue: Issue) {
  val createdAt: ZonedDateTime = issue.createdAt

  val daysAgo: FiniteDuration = ChronoUnit.DAYS.between(createdAt, ZonedDateTime.now()).days

  val prettyDays: String = {
    val d = daysAgo.toDays
    val s = PrettyOps.s(d)
    s"$d day$s ago"
  }
}
