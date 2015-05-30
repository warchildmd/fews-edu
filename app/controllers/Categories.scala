package controllers

import javax.inject.Inject

import models._

import play.api.mvc._

import play.api.libs.json.Json
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.libs.functional.syntax._

class Categories @Inject()(val categoriesRepo: models.Categories) extends Controller {

  implicit val categoryFormat = Json.format[models.Category]

  implicit def pageFormat[A : Format]: Format[Page[A]] =
  (
    (__ \ "results").format[Seq[A]] ~
    (__ \ "page").format[Int] ~
    (__ \ "offset").format[Long] ~
    (__ \ "total").format[Long]
  )(models.Page.apply, unlift(models.Page.unapply))

  def index() = Action { implicit s =>
    Ok(toJson(categoriesRepo.list()))
  }

}
