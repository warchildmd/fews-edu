package controllers

import javax.inject.Inject

import models._

import play.api.mvc._

import play.api.libs.json.Json
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class Articles @Inject()(protected val articlesRepo: models.Articles,
                         protected val akRepo: models.ArticleKeywords,
                         protected val keywordsRepo: models.Keywords) extends Controller {

  implicit val articleFormat = Json.format[models.Article]
  implicit val articleKeywordFormat = Json.format[models.ArticleKeyword]
  implicit val keywordFormat = Json.format[models.Keyword]

  implicit def pageFormat[A : Format]: Format[Page[A]] =
  (
    (__ \ "results").format[Seq[A]] ~
    (__ \ "page").format[Int] ~
    (__ \ "offset").format[Long] ~
    (__ \ "total").format[Long]
  )(models.Page.apply, unlift(models.Page.unapply))

  def get(id: Int) = Action { implicit s =>
    val article = articlesRepo.findById(id)
    val keywords = Await.result(akRepo.byArticle(Some(id)), Duration.Inf)
    val jsonKeywords = keywords.map((articleKeyword: ArticleKeyword) => {
      val keyword = Await.result(keywordsRepo.findById(articleKeyword.keywordId), Duration.Inf)
      toJson(articleKeyword).as[JsObject] + ("keyword" -> toJson(keyword))
    })
    val jsonArticle = toJson(Await.result(article, Duration.Inf)).as[JsObject] +
      ("value" -> toJson(articlesRepo.getPopularityIndex(Some(id)))) +
      ("keywords" -> toJson(jsonKeywords))
    Ok(toJson(jsonArticle))
  }

  def index = Action { implicit s =>
    Ok(toJson(articlesRepo.list()))
  }

  def popular = Action { implicit s =>
    val articles = articlesRepo.popular()
    val jsonArticles = articles.results.map((article: Article) => {
      toJson(article).as[JsObject] + ("value" -> toJson(articlesRepo.getPopularityIndex(article.id)))
    })
    val jsonResponse = Json.obj(
      "results" -> jsonArticles
    )
    Ok(toJson(jsonResponse))
  }

  def similar(id: Int) = Action { implicit s =>
    val articles = articlesRepo.similar(Some(id))
    val jsonArticles = articles.results.map((article: Article) => {
      toJson(article).as[JsObject] + ("value" -> toJson(articlesRepo.getPopularityIndex(article.id)))
    })
    val jsonResponse = Json.obj(
      "results" -> jsonArticles
    )
    Ok(toJson(jsonResponse))
  }

}
