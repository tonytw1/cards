package controllers

import model.DetectedImage
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._

@Singleton
class DetectController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  private val OneMegabyte = 1000000L

  def detect() = Action { implicit request: Request[AnyContent] =>
    implicit val diw = Json.writes[DetectedImage]
    (for {
      buffer <- request.body.asRaw
      bytes <- buffer.asBytes(maxLength = OneMegabyte)
    } yield {
      bytes.utf8String
      Ok(Json.toJson(Seq.empty[DetectedImage])) // TODO implement

    }).getOrElse {
      BadRequest(Json.toJson(s"Expected HTML on the request body of length less than $OneMegabyte"))
    }
  }

}
