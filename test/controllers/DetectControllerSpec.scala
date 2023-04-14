package controllers

import akka.util.ByteString
import model.DetectedImage
import org.apache.commons.io.IOUtils
import org.junit.Assert.assertEquals
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import parsing.ImageDetector
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._

import java.nio.charset.StandardCharsets

class DetectControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  private val controller = new DetectController(stubControllerComponents(), new ImageDetector())

  private implicit val dir = Json.reads[DetectedImage]

  "DetectController GET" should {
    "return bad request if no content found on request body" in {
      val response = controller.detect().apply(FakeRequest(POST, "/detect"))

      status(response) mustBe BAD_REQUEST
    }

    "return detected images if content found on request body" in {
      val requestWithHtmlBody = FakeRequest(POST, "/detect").
        withHeaders(CONTENT_TYPE -> "text/html").
        withRawBody(ByteString.fromString(loadAsString("page-with-og-image.html")))

      val response = controller.detect().apply(requestWithHtmlBody)

      status(response) mustBe OK
      val detectedImages = Json.parse(contentAsString(response)).as[Seq[DetectedImage]]
      detectedImages
      assertEquals(2, detectedImages.length)
      assertEquals("https://eyeofthefish.org/wp-content/uploads/2023/03/Gerard7.png", detectedImages.head.url)
    }
  }

  private def loadAsString(filename: String) = {
    IOUtils.toString(this.getClass.getClassLoader.getResourceAsStream(filename), StandardCharsets.UTF_8)
  }

}
