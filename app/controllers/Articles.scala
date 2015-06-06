package controllers

import javax.inject.Inject

import helpers.Page
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
                         protected val keywordsRepo: models.Keywords,
                         protected val auth: helpers.Auth) extends Controller {

  implicit val articleFormat = Json.format[models.Article]
  implicit val articleKeywordFormat = Json.format[models.ArticleKeyword]
  implicit val keywordFormat = Json.format[models.Keyword]

  implicit def pageFormat[A : Format]: Format[Page[A]] =
  (
    (__ \ "data").format[Seq[A]] ~
    (__ \ "current_page").format[Int] ~
    (__ \ "from").format[Int] ~
    (__ \ "to").format[Int] ~
    (__ \ "total").format[Int]
  )(Page.apply, unlift(helpers.Page.unapply))

  def get(id: Int) = Action { implicit s =>
    val article = articlesRepo.findById(id)
    val keywords = Await.result(akRepo.byArticle(Some(id)), Duration.Inf)
    val jsonKeywords = keywords.map((articleKeyword: ArticleKeyword) => {
      val keyword = Await.result(keywordsRepo.findById(articleKeyword.keywordId), Duration.Inf)
      toJson(articleKeyword).as[JsObject] + ("keyword" -> toJson(keyword))
    })
    var jsonArticle = toJson(Await.result(article, Duration.Inf)).as[JsObject] +
      ("value" -> toJson(articlesRepo.getPopularityIndex(Some(id)))) +
      ("keywords" -> toJson(jsonKeywords))

    val user = auth.getUser(s.headers.get("Auth-Token").getOrElse(""))
    if (user.nonEmpty) {
      articlesRepo.adjust(user.get.id, Some(id))

      jsonArticle += ("recommendation" -> toJson(articlesRepo.getScore(user.get.id, Some(id))))
    }

    Ok(toJson(jsonArticle))
  }

  def index = Action { implicit s =>
    val articles = articlesRepo.list()
    val jsonArticles = articles.data.map((article: Article) => {
      toJson(article).as[JsObject] + ("value" -> toJson(articlesRepo.getPopularityIndex(article.id)))
    })
    val jsonResponse = Json.obj(
      "data" -> jsonArticles
    )
    Ok(toJson(jsonResponse))
  }

  def popular = Action { implicit s =>
    val articles = articlesRepo.popular()
    val jsonArticles = articles.data.map((article: Article) => {
      toJson(article).as[JsObject] + ("value" -> toJson(articlesRepo.getPopularityIndex(article.id)))
    })
    val jsonResponse = Json.obj(
      "data" -> jsonArticles
    )
    Ok(toJson(jsonResponse))
  }

  def similar(id: Int) = Action { implicit s =>
    val articles = articlesRepo.betaSimilar(Some(id))
    val jsonArticles = articles.data.map((article: Article) => {
      toJson(article).as[JsObject] + ("value" -> toJson(articlesRepo.getPopularityIndex(article.id)))
    })
    val jsonResponse = Json.obj(
      "data" -> jsonArticles
    )
    Ok(toJson(jsonResponse))
  }

  def recommended() = Action { implicit s =>
    val user = auth.getUser(s.headers.get("Auth-Token").getOrElse(""))
    if (user.isEmpty) {
      val articles = articlesRepo.popular()
      val jsonArticles = articles.data.map((article: Article) => {
        toJson(article).as[JsObject] + ("value" -> toJson(articlesRepo.getPopularityIndex(article.id)))
      })
      val jsonResponse = Json.obj(
        "data" -> jsonArticles
      )
      Ok(toJson(jsonResponse))
    } else {
      val articles = articlesRepo.betaRecommend(user.get.id)
      val jsonArticles = articles.data.map((article: (Article, Option[Double])) => {
        toJson(article._1).as[JsObject] + ("value" -> toJson(articlesRepo.getPopularityIndex(article._1.id))) +
          ("recommendation" -> toJson(10.0 - article._2.get))
      })
      val jsonResponse = Json.obj(
        "data" -> jsonArticles
      )
      Ok(toJson(jsonResponse))
    }
  }

}
