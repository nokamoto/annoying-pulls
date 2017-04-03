package core

import com.typesafe.config.ConfigFactory
import core.ConfigOps.Implicits._
import org.scalatest.FlatSpec

import scala.concurrent.duration._

class ConfigOpsSpec extends FlatSpec {
  it should "return optional string" in {
    val config = ConfigFactory.parseString("""present = "x", absent = null""")
    assert(config.getOptionString("present") === Some("x"))
    assert(config.getOptionString("absent") === None)
    assert(config.getOptionString("undefined") === None)
  }

  it should "return finite duration in days" in {
    val key = "days"
    (1 to 1000).foreach { n =>
      assert(
        ConfigFactory
          .parseString(s"$key = $n days")
          .getFiniteDuration(key) === n.days)
    }
  }
}
