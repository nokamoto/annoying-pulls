package server

case class GithubOrg(owner: String, repos: List[GithubRepository] = Nil) extends GithubOwner {
  def repo(name: String, f: GithubRepository => GithubRepository): GithubOrg = copy(repos = f(newRepo(name)) :: repos)
}
