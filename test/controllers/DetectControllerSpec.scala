package controllers

import akka.util.ByteString
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import parsing.ImageDetector
import play.api.test.Helpers._
import play.api.test._

class DetectControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  private val controller = new DetectController(stubControllerComponents(), new ImageDetector())

  "DetectController GET" should {
    "return bad request if no content found on request body" in {
      val response = controller.detect().apply(FakeRequest(POST, "/detect"))

      status(response) mustBe BAD_REQUEST
    }

    "return ok if content found on request body" in {
      val requestWithHtmlBody = FakeRequest(POST, "/detect").
        withHeaders(CONTENT_TYPE -> "text/html").
        withRawBody(ByteString.fromString("some html"))

      val response = controller.detect().apply(requestWithHtmlBody)

      status(response) mustBe OK
    }
  }
}
