package controllers

import akka.util.ByteString
import org.apache.commons.io.IOUtils
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logging}

import java.io.{File, FileInputStream}
import java.net.URL
import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

@Singleton
class ThumbnailController @Inject()(val controllerComponents: ControllerComponents, val configuration: Configuration, wsClient: WSClient)
  extends BaseController with Logging with LocalFiles with ReasonableWaits {

  def thumbnail(url: String): Action[AnyContent] = Action.async {
    logger.info("Fetching pinned url: " + url)
    val validURL = Try(new URL(url)).toOption

    validURL.map { url =>
      val contentFile = new File(filePathForContent(url))
      if (contentFile.exists()) {
        val content = IOUtils.toByteArray(new FileInputStream(contentFile))
        val mayContentType = contentTypeOfPinned(url)

        val eventualResizedContent = resize(content)

        eventualResizedContent.map { resizedContent =>
          val contentLength = resizedContent.length
          logger.info(s"Returning thumbnail of local file ${filePathForContent(url)} with length $contentLength")
          Ok.sendEntity(HttpEntity.Strict(ByteString.apply(resizedContent), mayContentType))
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

  private def resize(content: Array[Byte]): Future[Array[Byte]] = {
    // Make a call to image proxy and return the result
    val imageProxyUrl = "http://imgproxy.example.com/AfrOrF3gWeDA6VOlDG4TzxMv39O7MXnF4CXpKUwGqRM/resize:fill:300:400:0/plain/http://example.com/images/curiosity.jpg"

    val imageProxyRequest = wsClient.url(imageProxyUrl).withRequestTimeout(TenSeconds)
    imageProxyRequest.get().map { r =>
      content
    }
  }

}
