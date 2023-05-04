package controllers

import akka.util.ByteString
import org.apache.commons.io.IOUtils
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logging}

import java.io.{File, FileInputStream, FileOutputStream}
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Try

@Singleton
class PinnedController @Inject()(val controllerComponents: ControllerComponents, val configuration: Configuration, wsClient: WSClient)
  extends BaseController with Logging with LocalFiles {

  def pinned(url: String) = Action.async { implicit request: Request[AnyContent] =>
    logger.info("Fetching pinned url: " + url)
    val validURL = Try(new URL(url)).toOption

    validURL.map { url =>
      val contentFile = new File(filePathForContent(url))
      if (contentFile.exists()) {
        val content = IOUtils.toByteArray(new FileInputStream(contentFile))
        val contentLength = content.length

        val mayContentType = {
          val mineTypeFile = new File(filepathForMimeType(url))
          if (mineTypeFile.isFile) {
            Some(IOUtils.toString(new FileInputStream(mineTypeFile), Charset.defaultCharset()))
          } else {
            None
          }
        }

        logger.info(s"Returning content from local file ${filePathForContent(url)} with length $contentLength")
        Future.successful(Ok.sendEntity(HttpEntity.Strict(ByteString.apply(content), mayContentType)))

      } else {
        logger.info(s"No local file ${filePathForContent(url)}")
        Future.successful(NotFound(Json.toJson("Not found")))
      }

    }.getOrElse {
      val message = "Expected a valid URL on url parameter"
      Future.successful(BadRequest(Json.toJson(message)))
    }
  }

  def pin(url: String) = Action.async { implicit request: Request[AnyContent] =>
    logger.info("Pinning url: " + url)
    val validURL = Try(new URL(url)).toOption

    validURL.map { url =>
      val contentFile = new File(filePathForContent(url))
      if (!contentFile.exists()) {
        logger.info("Fetching from: " + url)
        wsClient.url(url.toExternalForm).
          withRequestTimeout(Duration(10, TimeUnit.SECONDS)).get().map { r =>
          r.status match {
            case 200 =>
              val maybeContentType = r.header(CONTENT_TYPE)
              val content = r.bodyAsBytes.toArray
              IOUtils.write(content, new FileOutputStream(contentFile))

              maybeContentType.foreach { contentType =>
                IOUtils.write(contentType.getBytes, new FileOutputStream(new File(filepathForMimeType(url))))
              }

              logger.info(s"Wrote $url with content type $maybeContentType to local file ${contentFile.getAbsolutePath}")
              successfulPinOf

            case _ =>
              val message = s"Fetch of URL returned non 200 status: $r.status"
              logger.warn(message)
              InternalServerError(Json.toJson(message))
          }
        } recover {
          case e: Exception =>
            val message = s"Fetch of URL failed: ${e.getMessage}"
            logger.warn(message)
            InternalServerError(Json.toJson(message))
        }

      } else {
        logger.info(s"Not fetching $url already pinned in local file ${contentFile.getAbsolutePath}")
        Future.successful(successfulPinOf)
      }

    }.getOrElse {
      val message = "Expected a valid URL on url parameter"
      logger.warn(message)
      Future.successful(BadRequest(Json.toJson(message)))
    }
  }

  private def successfulPinOf = {
    Ok(Json.toJson("ok"))
  }

}
