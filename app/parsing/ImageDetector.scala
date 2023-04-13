package parsing

import model.DetectedImage
import org.jsoup.Jsoup

import java.net.URI
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.Try

class ImageDetector {

  private val imageProperties = Set("og:image", "twitter:image")

  private val validImageUrlSchemes = Set("http", "https")

  def detectImagesIn(html: String): Seq[DetectedImage] = {
    val doc = Jsoup.parse(html)

    val metaTagsWithProperties = doc.select("meta[property]").asScala.toSeq
    val metaTagsWithNames = doc.select("meta[name]").asScala.toSeq

    val allMetaTags = metaTagsWithProperties ++ metaTagsWithNames

    val imageMetaTags = allMetaTags.filter { e =>
      imageProperties.contains(e.attr("property")) ||
        imageProperties.contains(e.attr("name"))
    }
    imageMetaTags.flatMap { tag =>
      val proposedUrl = tag.attr("content")
      val parsedUri = Try(java.net.URI.create(proposedUrl)).toOption
      parsedUri.flatMap { uri: URI =>
        onlyFullQualified(uri).map { url =>
          DetectedImage(url = url.toURL.toExternalForm)
        }
      }
    }
  }

  private def onlyFullQualified(uri: URI) = {
    if (validImageUrlSchemes.contains(uri.getScheme)) {
      Some(uri)
    } else {
      None
    }
  }

}
