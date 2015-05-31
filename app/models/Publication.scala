package models

import javax.inject.Inject

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import tables.Tables

import org.joda.time.DateTime

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

import play.api.libs.concurrent.Execution.Implicits._

case class Publication(
  id: Option[Int] = None,
  name: String,
  description: String,
  link: String,
  categoryId: Int,
  createdAt: DateTime
  )


class Publications @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._
  val publications = TableQuery[PublicationsTable]

  def insert(publication: Publication): Future[Int] =  db.run((publications returning publications.map(_.id)) += publication)

  def update(id: Int, publication: Publication): Future[Unit] = {
    val publicationToUpdate: Publication = publication.copy(Some(id))
    db.run(publications.filter(_.id === id).update(publicationToUpdate)).map(_ => ())
  }

  def count(): Future[Int] = db.run(publications.length.result)

  def findById(id: Int): Future[Option[Publication]] =
    db.run(publications.filter(_.id === id).result.headOption)

}
