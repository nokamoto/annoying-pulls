package core

import java.time.ZonedDateTime

import core.DaysAgoSpec._
import org.scalatest.FlatSpec

class DaysAgoSpec extends FlatSpec {
  it should "return 0 day ago" in {
    daysAgo(now) { ago =>
      assert(ago.prettyDays === "0 day ago")
    }
  }

  it should "return 1 day ago" in {
    Seq(now.minusDays(1), now.minusDays(1).minusHours(23)).foreach { day =>
      daysAgo(day) { ago =>
        assert(ago.prettyDays === "1 day ago")
      }
    }
  }

  it should "return 2 days ago" in {
    daysAgo(now.minusDays(2)) { ago =>
      assert(ago.prettyDays === "2 days ago")
    }
  }
}

object DaysAgoSpec {
  private def now = ZonedDateTime.now()

  private def daysAgo(at: ZonedDateTime)(f: DaysAgo => Unit): Unit = {
    val ago = new DaysAgo {
      override protected[this] def from: ZonedDateTime = at
    }
    f(ago)
  }
}
