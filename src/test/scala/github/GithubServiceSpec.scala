package github

import org.scalatest.AsyncFlatSpec
import server.MockServersSetting

class GithubServiceSpec extends AsyncFlatSpec {
  it should "handle Link header" in {
    val setting = MockServersSetting(org = Some("mock-org"), pageSize = 2)
    val service = new GithubService(setting.context)

    setting.setOrg(_.pull(1, setting.now), _.pull(2, setting.now), _.pull(3, setting.now))

    setting.withServer {
      service.pulls().map { pulls =>
        assert(pulls.map(_.pulls.number).toSet === Set(1, 2, 3))
      }
    }
  }
}
