package server

import java.time.ZonedDateTime
import java.util.UUID

case class GithubRepository(owner: String, name: String, pulls: List[GithubPull]) {
  val fullName = s"$owner/$name"

  def pull(number: Int, createdAt: ZonedDateTime, f: GithubPull => GithubPull): GithubRepository = {
    val login = UUID.randomUUID().toString

    val p = GithubPull(
      fullName = fullName,
      number = number,
      title = s"$number ${UUID.randomUUID().toString}",
      url = s"http://localhost/$owner/$name/pulls/$number",
      createdAt = createdAt,
      labels = Nil,
      login = login,
      avatarUrl = s"http://localhost/$login/avatar")

    copy(pulls = f(p) :: pulls)
  }

  def pull(number: Int, createdAt: ZonedDateTime): GithubRepository = pull(number, createdAt, x => x)
}
