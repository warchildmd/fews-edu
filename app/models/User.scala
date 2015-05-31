package models

import javax.inject.Inject

import org.joda.time.DateTime
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import tables.Tables

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._

case class User(id: Option[Int] = None,
                username: String,
                password: String,
                status: String,
                createdAt: DateTime)


class Users @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  val users = TableQuery[UsersTable]

  def insert(user: User): Future[Int] = db.run((users returning users.map(_.id)) += user)

  def update(id: Int, user: User): Future[Unit] = {
    val userToUpdate: User = user.copy(Some(id))
    db.run(users.filter(_.id === id).update(userToUpdate)).map(_ => ())
  }

  def count(): Future[Int] = db.run(users.length.result)

  def findById(id: Int): Future[Option[User]] =
    db.run(users.filter(_.id === id).result.headOption)

  def findByUsername(id: String): Future[Option[User]] =
    db.run(users.filter(_.username === id).result.headOption)

}