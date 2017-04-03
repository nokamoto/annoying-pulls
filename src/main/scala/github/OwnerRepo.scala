package github

import github.json.Repo

case class OwnerRepo(owner: String, repo: String) {
  def asOwner: Owner = Owner(name = owner)

  def asRepo: Repo = Repo(name = repo, full_name = s"$owner/$repo")
}
