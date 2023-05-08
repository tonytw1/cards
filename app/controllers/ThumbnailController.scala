package controllers

import akka.util.ByteString
import org.apache.commons.codec.binary.Base64
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logging}

import java.io.File
import java.net.{URL, URLEncoder}
import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

@Singleton
class ThumbnailController @Inject()(val controllerComponents: ControllerComponents, val configuration: Configuration, wsClient: WSClient)
  extends BaseController with Logging with LocalFiles with ReasonableWaits {

  private val cardsUrl = configuration.get[String]("cards.url")
  private val imageProxyUrl = configuration.get[String]("imageproxy.url")

  def thumbnail(url: String): Action[AnyContent] = Action.async {
    logger.info("Fetching pinned url: " + url)
    val validURL = Try(new URL(url)).toOption

    validURL.map { url =>
      val contentFile = new File(filePathForContent(url))
      if (contentFile.exists()) {
        val eventualResizedContent = resize(url)

        val mayContentType = contentTypeOfPinned(url)
        eventualResizedContent.map { resizedContent =>
          val contentLength = resizedContent.length
          logger.info(s"Returning thumbnail of local file ${filePathForContent(url)} with length $contentLength")
          Ok.sendEntity(HttpEntity.Strict(ByteString.apply(resizedContent), mayContentType)).withHeaders("Cache-Control" -> "max-age=3600")
        }

      } else {
        logger.info(s"No local file ${filePathForContent(url)}")
        Future.successful(NotFound(Json.toJson("Not found")))
      }

    }.getOrElse {
      val message = "Expected a valid URL on url parameter"
      Future.successful(BadRequest(Json.toJson(message)))
    }
  }

  private def resize(url: URL): Future[Array[Byte]] = {
    // Make a call to image proxy and return the result
    // This ping pong back to ourselves is abit odd but isn't hurting
    val originUrl = cardsUrl + "/pinned?url=" + URLEncoder.encode(url.toExternalForm, "UTF-8")

    val signature = "no-signature"

    val width = 640
    val resizing = Seq(imageProxyUrl, signature, "resize:fill:" + width.toString, Base64.encodeBase64String(originUrl.getBytes)).mkString("/")

    val imageProxyRequest = wsClient.url(resizing).withRequestTimeout(TenSeconds)
    val eventualResponse = imageProxyRequest.get()
    eventualResponse.map { r =>
      r.status match {
        case 200 =>
          r.bodyAsBytes.toArray
        case _ =>
          logger.error(s"Error fetching thumbnail from image proxy: ${r.status} ${r.body}")
          Array.empty[Byte]
      }
    }
  }

}
