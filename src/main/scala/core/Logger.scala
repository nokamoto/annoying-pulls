package core

import org.slf4j.LoggerFactory

trait Logger {
  protected[this] val logger: org.slf4j.Logger =
    LoggerFactory.getLogger(this.getClass.getName.stripSuffix("$"))
}
