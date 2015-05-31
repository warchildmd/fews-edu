package models

import javax.inject.Inject

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import tables.Tables

import scala.concurrent.{Await, Future}

import play.api.libs.concurrent.Execution.Implicits._


case class UserKeyword(
  id: Option[Int] = None,
  userId: Int,
  keywordId: Int,
  value: Double
)

class UserKeywords @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  val userKeywords = TableQuery[UserKeywordsTable]

  def insert(userKeyword: UserKeyword): Future[Int] =  db.run((userKeywords returning userKeywords.map(_.id)) += userKeyword)

  def update(id: Int, userKeyword: UserKeyword): Future[Unit] = {
    val userKeywordToUpdate: UserKeyword = userKeyword.copy(Some(id))
    db.run(userKeywords.filter(_.id === id).update(userKeywordToUpdate)).map(_ => ())
  }

  def delete(id: Int): Future[Unit] =
    db.run(userKeywords.filter(_.id === id).delete).map(_ => ())

  def all(): Future[Seq[UserKeyword]] = db.run(userKeywords.result)

  def count(): Future[Int] = db.run(userKeywords.length.result)

  def findById(id: Int): Future[Option[UserKeyword]] =
    db.run(userKeywords.filter(_.id === id).result.headOption)

  def byArticle(id: Option[Int]): Future[Seq[UserKeyword]] =
    db.run(userKeywords.filter(_.userId === id).result)

}
