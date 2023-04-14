package parsing

import model.DetectedImage
import org.jsoup.Jsoup

import java.net.URI
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.Try

class ImageDetector {

  private val validImageUrlSchemes = Set("http", "https")

  def detectImagesIn(html: String): Seq[DetectedImage] = {
    val metaTags = metaTagsAsMap(html)

    val ogImage = for {
      ogImage <- detectedImageFrom(metaTags, "og:image")
    } yield {
      ogImage.copy(contentType = metaTags.get("og:image:type"))
    }

    val twitterImage = detectedImageFrom(metaTags, "twitter:image")
    Seq(ogImage, twitterImage).flatten
  }

  private def detectedImageFrom(metaTags: Map[String, String], imageIdentifier: String): Option[DetectedImage] = {
    for {
      imageContent <- metaTags.get(imageIdentifier)
      imageUri <- Try(java.net.URI.create(imageContent)).toOption
      fullyQualifiedImageUri <- onlyFullQualified(imageUri)
    } yield {
      DetectedImage(url = fullyQualifiedImageUri.toURL.toExternalForm)
    }
  }

  private def metaTagsAsMap(html: String): Map[String, String] = {
    val doc = Jsoup.parse(html)
    val allMetaTags = doc.select("meta").asScala.toSeq

    allMetaTags.flatMap { e =>
      for {
        // We are only interested in key value pair type tags
        identifier <- Seq(e.attr("property"), e.attr("name")).find(_.nonEmpty)
        content <- Option(e.attr("content"))
      } yield {
        (identifier, content)
      }
    }.toMap
  }

  private def onlyFullQualified(uri: URI): Option[URI] = {
    if (validImageUrlSchemes.contains(uri.getScheme)) {
      Some(uri)
    } else {
      None
    }
  }

}
