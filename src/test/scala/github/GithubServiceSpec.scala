package github

import org.scalatest.AsyncFlatSpec
import server.MockServersSetting

class GithubServiceSpec extends AsyncFlatSpec {
  it should "handle Link header" in {
    val setting = MockServersSetting(org = Some("mock-org"), pageSize = 2)
    val service = new GithubService(setting.context)

    setting.setOrg(_.pull(1, setting.now),
                   _.pull(2, setting.now),
                   _.pull(3, setting.now))

    setting.withServer {
      service.pulls().map { pulls =>
        assert(pulls.map(_.pulls.number).toSet === Set(1, 2, 3))
      }
    }
  }

  it should "get pull requests from include-repos" in {
    val owner = "mock-owner"
    val r1 = "mock-repo"
    val r2 = "mock-repo2"
    val setting = MockServersSetting(
      pageSize = 2,
      includeRepos = OwnerRepo(owner = owner, repo = r1) :: OwnerRepo(
        owner = owner,
        repo = r2) :: Nil)
    val service = new GithubService(setting.context)

    setting.forceSetOrg(owner,
                        _.pull(1, setting.now).copy(name = r1),
                        _.pull(2, setting.now),
                        _.pull(3, setting.now).copy(name = r2))

    setting.withServer {
      service.pulls().map { pulls =>
        assert(pulls.map(_.pulls.number).toSet === Set(1, 3))
      }
    }
  }
}
