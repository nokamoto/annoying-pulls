package server

trait GithubOwner {
  val owner: String

  val repos: List[GithubRepository]

  protected[this] def newRepo(name: String) = GithubRepository(owner = owner, name = name, pulls = Nil)
}
