package models

import javax.inject.Inject

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import tables.Tables

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

import play.api.libs.concurrent.Execution.Implicits._


case class ArticleKeyword(
  id: Option[Int] = None,
  articleId: Int,
  keywordId: Int,
  value: Double
)

class ArticleKeywords @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  val articleKeywords = TableQuery[ArticleKeywordsTable]

  def insert(articleKeyword: ArticleKeyword): Future[Int] =  db.run((articleKeywords returning articleKeywords.map(_.id)) += articleKeyword)

  def update(id: Int, articleKeyword: ArticleKeyword): Future[Unit] = {
    val articleKeywordToUpdate: ArticleKeyword = articleKeyword.copy(Some(id))
    db.run(articleKeywords.filter(_.id === id).update(articleKeywordToUpdate)).map(_ => ())
  }

  def delete(id: Int): Future[Unit] =
    db.run(articleKeywords.filter(_.id === id).delete).map(_ => ())

  def all(): Future[Seq[ArticleKeyword]] = db.run(articleKeywords.result)

  def count(): Future[Int] = db.run(articleKeywords.length.result)

  def findById(id: Int): Future[Option[ArticleKeyword]] =
    db.run(articleKeywords.filter(_.id === id).result.headOption)

  def list(page: Int = 0, pageSize: Int = 20, orderBy: Int = 1): Page[ArticleKeyword] = {

    val offset = pageSize * page
    val totalRows = Await.result(count(), Duration.Inf)

    val result = db.run(articleKeywords.drop(offset).take(pageSize).result)
    val list = Await.result(result, Duration.Inf)

    Page(list, page, offset, totalRows)
  }

  def byArticle(id: Option[Int]): Future[Seq[ArticleKeyword]] =
    db.run(articleKeywords.filter(_.articleId === id).result)

}
