package controllers

import model.DetectedImage
import parsing.ImageDetector
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._

@Singleton
class DetectController @Inject()(val controllerComponents: ControllerComponents, imageDetector: ImageDetector) extends BaseController {

  private val OneMegabyte = 2000000L

  def detect() = Action { implicit request: Request[AnyContent] =>
    implicit val diw = Json.writes[DetectedImage]
    (for {
      buffer <- request.body.asRaw
      bytes <- buffer.asBytes(maxLength = OneMegabyte)
    } yield {
      Ok(Json.toJson(imageDetector.detectImagesIn(bytes.utf8String)))

    }).getOrElse {
      BadRequest(Json.toJson(s"Expected HTML on the request body of length less than $OneMegabyte"))
    }
  }

}
