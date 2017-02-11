package server

case class GithubUser(owner: String, repos: List[GithubRepository] = Nil) extends GithubOwner {
  def repo(name: String, f: GithubRepository => GithubRepository): GithubUser = copy(repos = f(newRepo(name)) :: repos)
}
