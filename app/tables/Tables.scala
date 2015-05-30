package tables

import models._
import org.joda.time.DateTime
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile

import com.github.tototoshi.slick.MySQLJodaSupport._

/**
 * Created by Mihail on 5/29/2015.
 */
trait Tables { self: HasDatabaseConfigProvider[JdbcProfile] =>
    import driver.api._

    class ArticlesTable(tag: Tag) extends Table[Article](tag, "core_article") {
      def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
      def title = column[String]("title", O.SqlType("VARCHAR(128)"))
      def description = column[Option[String]]("description", O.SqlType("VARCHAR(512)"))
      def content = column[String]("content", O.SqlType("TEXT"))
      def link = column[String]("link")
      def image = column[Option[String]]("image")
      def publicationDate = column[DateTime]("publication_date")
      def feedId = column[Int]("feed_id")
      def hash = column[String]("hash", O.SqlType("VARCHAR(64)"))
      def crawlSessionId = column[Int]("crawl_session_id")
      def createdAt = column[DateTime]("created_at")

      def feed = foreignKey("article_feed_fk", feedId, TableQuery[FeedsTable])(_.id,
        onDelete=ForeignKeyAction.Restrict)
      def crawlSession = foreignKey("article_crawl_session_fk", crawlSessionId, TableQuery[CrawlSessionsTable])(_.id,
        onDelete=ForeignKeyAction.Cascade)
      def idx = index("hash_unique", hash, unique = true)

      def * = (id.?, title, description, content, link, image, publicationDate, feedId, hash,
        crawlSessionId, createdAt) <> (Article.tupled, Article.unapply)
    }


  class ArticleKeywordsTable(tag: Tag) extends Table[ArticleKeyword](tag, "core_article_keyword") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def articleId = column[Int]("article_id")
    def keywordId = column[Int]("keyword_id")
    def value = column[Double]("value", O.Default[Double](0.0))

    def article = foreignKey("ak_article_fk", articleId, TableQuery[ArticlesTable])(_.id,
      onDelete=ForeignKeyAction.Cascade)
    def keyword = foreignKey("ak_keyword_fk", keywordId, TableQuery[KeywordsTable])(_.id,
      onDelete=ForeignKeyAction.Cascade)

    def * = (id.?, articleId, keywordId, value) <> (ArticleKeyword.tupled, ArticleKeyword.unapply)
  }

  class KeywordsTable(tag: Tag) extends Table[Keyword](tag, "core_keyword") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def content = column[String]("content", O.SqlType("VARCHAR(128)"))
    def languageId = column[Int]("language_id")

    def language = foreignKey("keyword_language_fk", languageId, TableQuery[LanguagesTable])(_.id,
      onDelete=ForeignKeyAction.Restrict)
    def idx = index("content_index", content, unique = true)

    def * = (id.?, content, languageId) <> (Keyword.tupled, Keyword.unapply)
  }

  class LanguagesTable(tag: Tag) extends Table[Language](tag, "core_language") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def content = column[String]("content", O.SqlType("VARCHAR(64)"))

    def * = (id.?, content) <> (Language.tupled, Language.unapply)
  }

  class CategoriesTable(tag: Tag) extends Table[Category](tag, "core_category") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.SqlType("VARCHAR(64)"))
    def description = column[String]("description", O.SqlType("VARCHAR(512)"))

    def * = (id.?, name, description) <> (Category.tupled, Category.unapply)
  }

  class CrawlSessionsTable(tag: Tag) extends Table[CrawlSession](tag, "core_crawl_session") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def startTime = column[DateTime]("start_time")
    def endTime = column[DateTime]("end_time")

    def * = (id.?, startTime, endTime.?) <> (CrawlSession.tupled, CrawlSession.unapply)
  }

  class FeedsTable(tag: Tag) extends Table[Feed](tag, "core_feed") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.SqlType("VARCHAR(64)"))
    def description = column[String]("description", O.SqlType("VARCHAR(512)"))
    def link = column[String]("link", O.SqlType("VARCHAR(256)"))
    def languageId = column[Int]("language_id")
    def categoryId = column[Int]("category_id")
    def publicationId = column[Int]("publication_id")
    def lastRead = column[DateTime]("last_read")
    def createdAt = column[DateTime]("created_at")

    def language = foreignKey("feed_language_fk", languageId, TableQuery[LanguagesTable])(_.id,
      onDelete=ForeignKeyAction.Restrict)
    def category = foreignKey("feed_category_fk", categoryId, TableQuery[CategoriesTable])(_.id,
      onDelete=ForeignKeyAction.Restrict)
    def publication = foreignKey("feed_publication_fk", publicationId, TableQuery[PublicationsTable])(_.id,
      onDelete=ForeignKeyAction.Restrict)

    def * = (id.?, name, description, link, languageId, categoryId, publicationId,
      lastRead, createdAt) <> (Feed.tupled, Feed.unapply)
  }


  class PublicationsTable(tag: Tag) extends Table[Publication](tag, "core_publication") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.SqlType("VARCHAR(64)"))
    def description = column[String]("description", O.SqlType("VARCHAR(512)"))
    def link = column[String]("link", O.SqlType("VARCHAR(256)"))
    def categoryId = column[Int]("category_id")
    def createdAt = column[DateTime]("created_at")

    def category = foreignKey("publication_category_fk", categoryId, TableQuery[CategoriesTable])(_.id,
      onDelete=ForeignKeyAction.Restrict)

    def * = (id.?, name, description, link, categoryId, createdAt) <> (Publication.tupled, Publication.unapply)
  }

}
