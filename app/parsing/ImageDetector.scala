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

    val allMetaTags = doc.select("meta").asScala.toSeq

    val imageMetaTags = allMetaTags.filter { e =>
      val identifier = Seq(Option(e.attr("property")), Option(e.attr("name"))).flatten
      identifier.exists(imageProperties.contains)
    }
    imageMetaTags.flatMap { tag =>
      val proposedUrl = tag.attr("content")
      val parsedUri = Try(java.net.URI.create(proposedUrl)).toOption
      parsedUri.flatMap { uri =>
        onlyFullQualified(uri).map { url =>
          DetectedImage(url = url.toURL.toExternalForm, contentType = None)
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
