package models


import javax.inject.Inject

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import tables.Tables

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

import play.api.libs.concurrent.Execution.Implicits._

case class Keyword(
  id: Option[Int] = None,
  content: String,
  languageId: Int
  )

class Keywords @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  val keywords = TableQuery[KeywordsTable]

  def insert(keyword: Keyword): Future[Int] =  db.run((keywords returning keywords.map(_.id)) += keyword)

  def update(id: Int, keyword: Keyword): Future[Unit] = {
    val keywordToUpdate: Keyword = keyword.copy(Some(id))
    db.run(keywords.filter(_.id === id).update(keywordToUpdate)).map(_ => ())
  }

  def delete(id: Int): Future[Unit] =
    db.run(keywords.filter(_.id === id).delete).map(_ => ())

  def all(): Future[Seq[Keyword]] = db.run(keywords.result)

  def count(): Future[Int] = db.run(keywords.length.result)

  def findById(id: Int): Future[Option[Keyword]] =
    db.run(keywords.filter(_.id === id).result.headOption)

  def list(page: Int = 0, pageSize: Int = 20, orderBy: Int = 1): Page[Keyword] = {

    val offset = pageSize * page
    val totalRows = Await.result(count(), Duration.Inf)

    val result = db.run(keywords.drop(offset).take(pageSize).result)
    val list = Await.result(result, Duration.Inf)

    Page(list, page, offset, totalRows)
  }

  def findByContent(content: String): Future[Option[Keyword]] =
    db.run(keywords.filter(_.content === content).result.headOption)


}
