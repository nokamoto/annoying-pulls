package core

trait AttachmentTitle {
  protected[this] def repoFullName: String

  protected[this] def pullTitle: String

  protected[this] def hashNumber: Long

  def attachmentTitle: String = s"[$repoFullName] $pullTitle #$hashNumber"
}
