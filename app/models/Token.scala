package models

import javax.inject.Inject

import org.joda.time.DateTime
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import tables.Tables

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._

case class Token(id: Option[Int] = None,
                token: String,
                userId: Int,
                createdAt: DateTime)


class Tokens @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  val sessions = TableQuery[TokensTable]

  def insert(token: Token): Future[Int] = db.run((sessions returning sessions.map(_.id)) += token)

  def update(id: Int, session: Token): Future[Unit] = {
    val sessionToUpdate: Token = session.copy(Some(id))
    db.run(sessions.filter(_.id === id).update(sessionToUpdate)).map(_ => ())
  }

  def delete(id: Int): Future[Unit] =
    db.run(sessions.filter(_.id === id).delete).map(_ => ())

  def count(): Future[Int] = db.run(sessions.length.result)

  def findById(id: Int): Future[Option[Token]] =
    db.run(sessions.filter(_.id === id).result.headOption)

  def findByContent(content: String): Future[Option[Token]] =
    db.run(sessions.filter(_.token === content).result.headOption)

}