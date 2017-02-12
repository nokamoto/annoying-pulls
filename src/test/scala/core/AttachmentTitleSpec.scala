package core

import org.scalatest.FlatSpec

import scala.util.Random

class AttachmentTitleSpec extends FlatSpec {
  it should "return [:full_name] :title #:number" in {
    val n = Random.nextInt(10000)

    val sut = new AttachmentTitle {
      override protected[this] def repoFullName = ":owner/:repo"

      override protected[this] def hashNumber: Long = n

      override protected[this] def pullTitle = ":title"
    }

    assert(sut.attachmentTitle === s"[:owner/:repo] :title #$n")
  }
}
