package slack

import core._
import helper.{DefaultFutures, DummyPullRequest}
import org.scalatest.FlatSpec
import slack.SlackServiceSpec.withService

import scala.concurrent.ExecutionContext

/**
  * sbt "set testOptions in Test := Nil" "testOnly slack.SlackServiceSpec"
  */
class SlackServiceSpec
    extends FlatSpec
    with DefaultFutures
    with DummyPullRequest {
  private[this] def post(pulls: Context => Seq[PullRequest]): Unit = {
    withService { (context, service) =>
      whenReady(service.webhook(pulls(context).toList))(_ => ())
    }
  }

  it should "post empty pulls to incoming webhook" taggedAs SlackTest in {
    post(_ => Nil)
  }

  it should "post good, warning, danger pulls to incoming webhook" taggedAs SlackTest in {
    post { context =>
      import context._

      pullRequest(createdAt = now.minusDays(1), title = "good", number = 2) ::
        pullRequest(createdAt = now.minusDays(slack.warningAfter.toDays),
                    title = "warning",
                    number = 3) ::
        pullRequest(createdAt = now.minusDays(slack.dangerAfter.toDays),
                    title = "danger",
                    number = 4) :: Nil
    }
  }

  it should "post pulls with the number of comments to incoming webhook" taggedAs SlackTest in {
    post { context =>
      import context._

      pullRequest(createdAt = now, title = "0 comment", number = 2) ::
        pullRequest(createdAt = now,
                    title = "1 comment",
                    number = 3,
                    comments = 1) ::
        pullRequest(createdAt = now,
                    title = "1 review comment",
                    number = 4,
                    reviewComments = 1) ::
        pullRequest(createdAt = now,
                    title = "1 comment and 1 review comment",
                    number = 5,
                    comments = 1,
                    reviewComments = 1) :: Nil
    }
  }
}

object SlackServiceSpec {
  def withService(f: (Context, SlackService) => Unit)(
      implicit ec: ExecutionContext): Unit = {
    val context = Context()
    val service = new SlackService(context)
    try {
      f(context, service)
    } finally {
      context.shutdown()
    }
  }
}
