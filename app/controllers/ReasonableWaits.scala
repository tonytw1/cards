package controllers

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

trait ReasonableWaits {

  val TenSeconds = Duration(10, TimeUnit.SECONDS)

}
