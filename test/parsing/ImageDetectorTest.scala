package parsing

import model.DetectedImage
import org.apache.commons.io.IOUtils
import org.junit.Assert.{assertEquals, assertFalse}
import org.junit.Test

import java.nio.charset.StandardCharsets

class ImageDetectorTest {

  val imageDetector = new ImageDetector()

  @Test
  def shouldReturnEmptyListIfNoImagesFound(): Unit = {
    assertEquals(Seq.empty, imageDetector.detectImagesIn("Nothing to see here"))
  }

  @Test
  def canDetectTwitterImageFromMetaTags(): Unit = {
    val detected = imageDetector.detectImagesIn(loadAsString("rimutaka-incline-railway-news.html"))
    assertEquals(Some("https://www.rimutaka-incline-railway.org.nz/sites/default/files/2020-12/20201017-a1328-IMG_6380.JPG"),
      detected.headOption.map(_.url))
  }

  @Test
  def canDetectOgImageFromMetaNameTags(): Unit = {
    val detected = imageDetector.detectImagesIn(loadAsString("page-with-og-image.html"))
    assertEquals(Some("https://eyeofthefish.org/wp-content/uploads/2023/03/Gerard7.png"), detected.headOption.map(_.url))
  }

  @Test
  def canDetectOgImageFromMetaPropertyTags(): Unit = {
    val detected = imageDetector.detectImagesIn(loadAsString("page-with-og-image-property.html"))
    assertEquals(Some("https://www.wellingtonjudo.org.nz/wp-content/uploads/2018/05/tn_DSCF9240.jpg"), detected.headOption.map(_.url))
  }

  @Test
  def shouldIgnoreNoneAbsoluteOgImageUrls(): Unit = {
    val detected = imageDetector.detectImagesIn(loadAsString("page-with-relative-og-image.html"))
    assertFalse(detected.contains(DetectedImage(url = "/assets/Uploads/ShareImage/Lionesses-Djane-left-and-Zahra-right.JPG")))
  }

  @Test
  def shouldCaptureAdditionalOGDataWhereAvailable(): Unit = {
    val detected = imageDetector.detectImagesIn(loadAsString("page-with-og-image-property.html"))

    assertEquals(Some("image/jpeg"), detected.head.contentType)
  }

  private def loadAsString(filename: String) = {
    IOUtils.toString(this.getClass.getClassLoader.getResourceAsStream(filename), StandardCharsets.UTF_8)
  }

}
