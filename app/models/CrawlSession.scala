package models

import javax.inject.Inject

import org.joda.time.DateTime
import play.api.db.slick._
import slick.driver.JdbcProfile
import tables.Tables

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

import play.api.libs.concurrent.Execution.Implicits._

case class CrawlSession(
  id: Option[Int] = None,
  startTime: DateTime,
  endTime: Option[DateTime]
  )

class CrawlSessions @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._
  val crawlSessions = TableQuery[CrawlSessionsTable]

  def insert(crawlSession: CrawlSession): Future[Int] =  db.run((crawlSessions returning crawlSessions.map(_.id)) += crawlSession)

  def update(id: Int, crawlSession: CrawlSession): Future[Unit] = {
    val crawlSessionToUpdate: CrawlSession = crawlSession.copy(Some(id))
    db.run(crawlSessions.filter(_.id === id).update(crawlSessionToUpdate)).map(_ => ())
  }

  def delete(id: Int): Future[Unit] =
    db.run(crawlSessions.filter(_.id === id).delete).map(_ => ())

  def count(): Future[Int] =
    db.run(crawlSessions.length.result)

  def findById(id: Int): Future[Option[CrawlSession]] =
    db.run(crawlSessions.filter(_.id === id).result.headOption)

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1): Page[CrawlSession] = {

    val offset = pageSize * page
    val totalRows = Await.result(count(), Duration.Inf)

    val result = db.run(crawlSessions.drop(offset).take(pageSize).result)
    val list = Await.result(result, Duration.Inf)

    Page(list, page, offset, totalRows)
  }

}
