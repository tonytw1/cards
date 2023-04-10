package parsing

import model.DetectedImage
import org.jsoup.Jsoup

import scala.jdk.CollectionConverters.CollectionHasAsScala

class ImageDetector {

  private val imageProperties = Set("og:image", "twitter:image")

  def detectImagesIn(html: String): Seq[DetectedImage] = {
    val doc = Jsoup.parse(html)

    val metaTagsWithProperties = doc.select("meta[property]").asScala.toSeq
    val metaTagsWithNames = doc.select("meta[name]").asScala.toSeq

    val allMetaTags = metaTagsWithProperties ++ metaTagsWithNames

    val imageMetaTags = allMetaTags.filter { e =>
      imageProperties.contains(e.attr("property")) ||
      imageProperties.contains(e.attr("name"))
    }
    imageMetaTags.map { tag =>
      DetectedImage(url = tag.attr("content"))
    }
  }

}
