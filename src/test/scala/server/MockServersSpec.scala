package server

import github.json.Repo
import org.scalatest.{Assertion, AsyncFlatSpec}
import play.api.libs.json.JsSuccess
import play.api.libs.ws.WSResponse
import server.MockServersSpec.withGithubServer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MockServersSpec extends AsyncFlatSpec {
  "GithubServer" should "return repos pages" in {
    val org = "mock-org"
    val r1 = "r1"
    val r2 = "r2"
    val r3 = "r3"
    val r4 = "r4"
    val setting = MockServersSetting(org = Some(org), pageSize = 3)

    setting.setOrg(_.copy(name = r1), _.copy(name = r2), _.copy(name = r3), _.copy(name = r4))

    withGithubServer(setting) { get =>
      for {
        (link, repos) <- get(s"/orgs/$org/repos").map { res =>
          (res.header("LINK"), res.json.validate[List[Repo]].map(_.map(_.name)))
        }
        (nextLink, nextRepos) <- get(s"/orgs/$org/repos/1").map { res =>
          (res.header("LINK"), res.json.validate[List[Repo]].map(_.map(_.name)))
        }
      } yield {
        assert(link === Some(s"""<${setting.context.github.api}/orgs/mock-org/repos/1>; rel="next", <${setting.context.github.api}>; rel="dummy""""))
        assert(nextLink === None)

        assert(repos === JsSuccess(r4 :: r3 :: r2 :: Nil))
        assert(nextRepos === JsSuccess(r1 :: Nil))
      }
    }
  }
}

object MockServersSpec {
  def withGithubServer(setting: MockServersSetting)
                      (f: (String => Future[WSResponse]) => Future[Assertion]): Future[Assertion] = {
    setting.withServer(f(path => setting.context.ws.url(s"${setting.context.github.api}$path").get()))
  }
}
