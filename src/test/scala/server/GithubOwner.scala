package server

import github.Owner

trait GithubOwner {
  val owner: String

  val repos: List[GithubRepository]

  protected[this] def newRepo(name: String) = GithubRepository(owner = owner, name = name, pulls = Nil)

  def toOwner: Owner = Owner(owner)

  def ownerRepos: List[(String, GithubRepository)] = repos.map(repo => (owner, repo))
}
