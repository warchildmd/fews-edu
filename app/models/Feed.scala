package models

// import scala.slick.driver.JdbcDriver.simple._

import javax.inject.Inject

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import tables.Tables
import org.joda.time.DateTime

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

case class Feed(
  id: Option[Int] = None,
  name: String,
  description: String,
  link: String,
  languageId: Int,
  categoryId: Int,
  publicationId: Int,
  lastRead: DateTime,
  createdAt: DateTime
  )

class Feeds @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._
  val feeds = TableQuery[FeedsTable]

  def count(): Future[Int] =
    db.run(feeds.length.result)

  def findById(id: Int): Future[Option[Feed]] =
    db.run(feeds.filter(_.id === id).result.headOption)

  def all(): Future[Seq[Feed]] = db.run(feeds.result)

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1): Page[Feed] = {

    val offset = pageSize * page
    val totalRows = Await.result(count(), Duration.Inf)

    val result = db.run(feeds.drop(offset).take(pageSize).result)
    val list = Await.result(result, Duration.Inf)

    Page(list, page, offset, totalRows)
  }

}
