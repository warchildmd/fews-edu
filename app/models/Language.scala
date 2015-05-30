package models

import javax.inject.Inject

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import tables.Tables

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

import play.api.libs.concurrent.Execution.Implicits._


case class Language(
  id: Option[Int] = None,
  content: String
  )

class Languages @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._
  val languages = TableQuery[LanguagesTable]

  def insert(language: Language): Future[Int] =
    db.run((languages returning languages.map(_.id)) += language)

  def update(id: Int, language: Language) {
    val languageToUpdate: Language = language.copy(Some(id))
    db.run(languages.filter(_.id === id).update(languageToUpdate)).map(_ => ())
  }

  def count(): Future[Int] =
    db.run(languages.length.result)

  def findById(id: Int): Future[Option[Language]] =
    db.run(languages.filter(_.id === id).result.headOption)

  def findByContent(content: String): Future[Option[Language]] =
    db.run(languages.filter(_.content === content).result.headOption)

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1): Page[Language] = {

    val offset = pageSize * page
    val totalRows = Await.result(count(), Duration.Inf)

    val result = db.run(languages.drop(offset).take(pageSize).result)
    val list = Await.result(result, Duration.Inf)

    Page(list, page, offset, totalRows)
  }

}
