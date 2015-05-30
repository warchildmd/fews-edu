package models

import javax.inject.Inject

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import tables.Tables

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


case class Category(
  id: Option[Int] = None,
  name: String,
  description: String
  )

class Categories @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._
  val categories = TableQuery[CategoriesTable]

  def count(): Future[Int] =
    db.run(categories.length.result)

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1): Page[Category] = {

    val offset = pageSize * page
    val totalRows = Await.result(count(), Duration.Inf)

    val result = db.run(categories.drop(offset).take(pageSize).result)
    val list = Await.result(result, Duration.Inf)

    Page(list, page, offset, totalRows)
  }

}
