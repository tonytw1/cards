package parsing

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageDetectorTest {

  @Test
  def shouldReturnEmptyListIfNoImagesFound(): Unit = {
    assertEquals(Seq.empty, new ImageDetector().detectImagesIn("Nothing to see here"))
  }

}
