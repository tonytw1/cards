package controllers

import akka.util.ByteString
import org.apache.commons.io.IOUtils
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{Configuration, Logging}

import java.io.{File, FileInputStream}
import java.net.URL
import java.nio.charset.Charset
import javax.inject._
import scala.concurrent.Future
import scala.util.Try

@Singleton
class ThumbnailController @Inject()(val controllerComponents: ControllerComponents, val configuration: Configuration)
  extends BaseController with Logging with LocalFiles {

  private val pinnedFolder = configuration.get[String]("pinned.folder")

  def thumbnail(url: String) = Action.async { implicit request: Request[AnyContent] =>
    logger.info("Fetching pinned url: " + url)
    val validURL = Try(new URL(url)).toOption

    validURL.map { url =>
      val contentFile = new File(filePathForContent(url))
      if (contentFile.exists()) {
        val content = IOUtils.toByteArray(new FileInputStream(contentFile))
        val mayContentType = {
          val mineTypeFile = new File(filepathForMimeType(url))
          if (mineTypeFile.isFile) {
            Some(IOUtils.toString(new FileInputStream(mineTypeFile), Charset.defaultCharset()))
          } else {
            None
          }
        }

        val resizedContent = resize(content)

        val contentLength = resizedContent.length
        logger.info(s"Returning thumbnail of local file ${filePathForContent(url)} with length $contentLength")
        Future.successful(Ok.sendEntity(HttpEntity.Strict(ByteString.apply(resizedContent), mayContentType)))

      } else {
        logger.info(s"No local file ${filePathForContent(url)}")
        Future.successful(NotFound(Json.toJson("Not found")))
      }

    }.getOrElse {
      val message = "Expected a valid URL on url parameter"
      Future.successful(BadRequest(Json.toJson(message)))
    }
  }
  private def resize(content: Array[Byte]): Array[Byte] = {
    // TODO Given the source image file pass it through image proxy
    content
  }

}
