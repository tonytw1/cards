package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.Helpers._
import play.api.test._

class PinnedControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "PinnedController POST" should {

    "return HTTP 200 on success" in {
      val controller = app.injector.instanceOf[PinnedController]
      val pin = controller.pin(url = "https://wellington.gen.nz/static/civic8.jpg").apply(FakeRequest(POST, "/pinned"))

      status(pin) mustBe OK
    }

    "return bad request for invalid URL" in {
      val controller = app.injector.instanceOf[PinnedController]
      val pin = controller.pin(url = "Not a parsable url").apply(FakeRequest(POST, "/pinned"))

      status(pin) mustBe BAD_REQUEST
    }

    "is idempotent on repeat pin" in {
      val controller = app.injector.instanceOf[PinnedController]
      val pin = controller.pin(url = "https://wellington.gen.nz/static/civic8.jpg").apply(FakeRequest(POST, "/pinned"))
      status(pin) mustBe OK

      val repin = controller.pin(url = "https://wellington.gen.nz/static/civic8.jpg").apply(FakeRequest(POST, "/pinned"))
      status(repin) mustBe OK
      contentAsString(repin) mustBe "\"ok\""
    }
  }

  "PinnedController GET" should {
    "return locally pinned image" in {
      val controller = app.injector.instanceOf[PinnedController]
      val pin = controller.pin(url = "https://wellington.gen.nz/static/civic8.jpg").apply(FakeRequest(POST, "/pinned"))
      status(pin) mustBe OK

      val readback = controller.pinned(url = "https://wellington.gen.nz/static/civic8.jpg").apply(FakeRequest(GET, "/pinned"))

      status(readback) mustBe OK
      contentType(readback) mustBe Some("image/jpeg")
      contentAsBytes(readback).size mustBe 15633
    }
  }
}
