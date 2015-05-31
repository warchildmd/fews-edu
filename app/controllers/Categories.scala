package controllers

import javax.inject.Inject

import helpers.Page
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
    (__ \ "data").format[Seq[A]] ~
    (__ \ "current_page").format[Int] ~
    (__ \ "from").format[Int] ~
    (__ \ "to").format[Int] ~
    (__ \ "total").format[Int]
  )(Page.apply, unlift(helpers.Page.unapply))

  def index() = Action { implicit s =>
    Ok(toJson(categoriesRepo.list()))
  }

}
