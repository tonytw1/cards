package parsing

import model.DetectedImage
import org.jsoup.Jsoup

class ImageDetector {

  def detectImagesIn(html: String): Seq[DetectedImage] = {
    val doc = Jsoup.parse(html)
    Seq.empty
  }

}
