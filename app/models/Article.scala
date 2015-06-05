package models

import helpers.Page
import org.joda.time.DateTime
import tables.Tables

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile

import com.github.tototoshi.slick.MySQLJodaSupport._

case class Article(id: Option[Int] = None,
                   title: String,
                   description: Option[String],
                   content: String,
                   link: String,
                   image: Option[String],
                   publicationDate: DateTime,
                   feedId: Int,
                   hash: String,
                   crawlSessionId: Int,
                   createdAt: DateTime)

class Articles @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  val articles = TableQuery[ArticlesTable]

  def insert(article: Article): Future[Int] = db.run((articles returning articles.map(_.id)) += article)

  def update(id: Int, article: Article): Future[Unit] = {
    val articleToUpdate: Article = article.copy(Some(id))
    db.run(articles.filter(_.id === id).update(articleToUpdate)).map(_ => ())
  }

  def delete(id: Int): Future[Unit] =
    db.run(articles.filter(_.id === id).delete).map(_ => ())

  def all(): Future[Seq[Article]] = db.run(articles.result)

  def count(): Future[Int] = db.run(articles.length.result)

  def findById(id: Int): Future[Option[Article]] =
    db.run(articles.filter(_.id === id).result.headOption)

  def list(page: Int = 0, pageSize: Int = 20, orderBy: Int = 1): Page[Article] = {

    val offset = pageSize * page
    val total = Await.result(count(), Duration.Inf)

    val result = db.run(articles.sortBy(_.publicationDate.desc).drop(offset).take(pageSize).result)
    val list = Await.result(result, Duration.Inf)

    Page(list, page, offset, offset + list.length, total)
  }

  def popular(): Page[Article] = {
    val articleKeywords = TableQuery[ArticleKeywordsTable]
    val articleKeywordsQuery =
      (
        for {
          (article, articleKeyword) <- articles join articleKeywords on (_.id === _.articleId)
          if article.publicationDate >= new DateTime().minusDays(3)
        } yield (articleKeyword.keywordId, articleKeyword.value)
        ).groupBy(_._1)
    val articleKeywordsQueryValues = articleKeywordsQuery.map { case (keywordId, line) =>
      (keywordId, line.map(_._2).sum)
    }
    val globalQuery =
      (
        for {
          (articleKeyword, kValue) <- articleKeywords join articleKeywordsQueryValues on (_.keywordId === _._1)
        } yield (articleKeyword.articleId, kValue._2)
        ).groupBy(_._1)
    val globalQuerySum = globalQuery.map { case (articleId, line) =>
      (articleId, line.map(_._2).sum)
    }
    val sorted = globalQuerySum.sortBy(_._2.desc).take(20)
    val sortedArticles = for {
      (sv, article) <- sorted join articles on (_._1 === _.id)
    } yield article

    val list = Await.result(db.run(sortedArticles.result), Duration.Inf)

    Page(list, 1, 0, list.length, list.length)
  }

  def similar(articleId: Option[Int]): Page[Article] = {
    val articleKeywords = TableQuery[ArticleKeywordsTable]
    val keywordIds = articleKeywords.filter(_.articleId === articleId).map(_.keywordId)

    val values = (
      for {
        (articleKeyword, article) <- articleKeywords join articles on (_.articleId === _.id)
        if articleKeyword.keywordId in keywordIds
        if article.publicationDate >= new DateTime().minusDays(3)
      } yield (article.id, articleKeyword.value)).groupBy(_._1)

    val globalQuerySum = values.map { case (articleId, line) =>
      (articleId, line.map(_._2).sum)
    }

    val sorted = globalQuerySum.sortBy(_._2.desc).take(5)

    val sortedArticles = for {
      (sv, article) <- sorted join articles on (_._1 === _.id)
    } yield article

    val list = Await.result(db.run(sortedArticles.result), Duration.Inf)

    Page(list, 1, 0, list.length, list.length)
  }

  def betaSimilar(articleId: Option[Int]): Page[Article] = {
    val articleKeywords = TableQuery[ArticleKeywordsTable]
    val dbAbs = SimpleFunction.unary[Double, Double]("abs")
    val threeDaysAgo = new DateTime().minusDays(3)

    val myKeywords = articleKeywords.filter(_.articleId === articleId)

    val freshArticles = articles.filter(_.publicationDate >= threeDaysAgo)

    val inJoin = for {
      article <- freshArticles
      keyword <- myKeywords
    } yield (article.id, keyword.keywordId)

    val emptyAK = new ArticleKeyword(None, 0, 0, 0)

    val valuesJoin = for {
      (ij, ak) <- inJoin joinLeft articleKeywords on ((ij, ak) => {
        ij._1 === ak.articleId && ij._2 === ak.keywordId
      })
    } yield (ij._1, ij._2, ak.map(_.value))

    val normalize = (
      for {
        (values, keywords) <- valuesJoin join myKeywords on (_._2 === _.keywordId)
      } yield (values._1, dbAbs(values._3.getOrElse(0.0) - keywords.value))).groupBy(_._1)

    val globalQuerySum = normalize.map { case (articleId, line) =>
      (articleId, line.map(_._2).sum)
    }

    val sorted = globalQuerySum.sortBy(_._2.asc).take(5)

    val sortedArticles = for {
      (sv, article) <- sorted join articles on (_._1 === _.id)
    } yield article

    val list = Await.result(db.run(sortedArticles.result), Duration.Inf)

    Page(list, 1, 0, list.length, list.length)
  }

  def adjust(userId: Option[Int], articleId: Option[Int]) {
    val akQuery = TableQuery[ArticleKeywordsTable]
    val ukQuery = TableQuery[UserKeywordsTable]
    val alpha = 0.05

    val akList = Await.result(db.run(akQuery.filter(_.articleId === articleId).result), Duration.Inf)

    akList.foreach { ak =>
      val uk = Await.result(db.run(ukQuery.filter(_.userId === userId).filter(_.keywordId === ak.keywordId).result.headOption),
        Duration.Inf)
      if (uk.isEmpty) {
        val newValue = 0.5 + alpha * (ak.value - 0.5)
        val newUK = new UserKeyword(None, userId.get, ak.keywordId, newValue)
        db.run(ukQuery += newUK)
      } else {
        val newValue = uk.get.value + alpha * (ak.value - uk.get.value)
        val newUK = new UserKeyword(uk.get.id, userId.get, ak.keywordId, newValue)
        db.run(ukQuery.filter(_.id === uk.get.id).update(newUK)).map(_ => ())
      }
    }
  }

  def getScore(userId: Option[Int], articleId: Option[Int]): Double = {
    val akQuery = TableQuery[ArticleKeywordsTable]
    val ukQuery = TableQuery[UserKeywordsTable]

    val akList = Await.result(db.run(akQuery.filter(_.articleId === articleId).result), Duration.Inf)
    var distance: Double = 0
    akList.foreach { ak =>
      val uk = Await.result(db.run(ukQuery.filter(_.userId === userId).filter(_.keywordId === ak.keywordId).result.headOption),
        Duration.Inf)
      if (uk.isEmpty) {
        distance += math.abs(ak.value - 0.5)
      } else {
        distance += math.abs(ak.value - uk.get.value)
      }
    }
    10.0 - distance
  }

  def recommend(userId: Option[Int]): Page[Article] = {
    val articleKeywords = TableQuery[ArticleKeywordsTable]
    val userKeywords = TableQuery[UserKeywordsTable]
    val dbAbs = SimpleFunction.unary[Double, Double]("abs")
    val oneDayAgo = new DateTime().minusDays(1)

    val myKeywords = userKeywords.filter(_.userId === userId).sortBy(_.value.desc).take(100)

    val freshArticles = articles.filter(_.publicationDate >= oneDayAgo)

    val inJoin = for {
      article <- freshArticles
      keyword <- myKeywords
    } yield (article.id, keyword.keywordId)

    val valuesJoin = for {
      (ij, ak) <- inJoin joinLeft articleKeywords on ((ij, ak) => {
        ij._1 === ak.articleId && ij._2 === ak.keywordId
      })
    } yield (ij._1, ij._2, ak.map(_.value))

    val normalize = (
      for {
        (values, keywords) <- valuesJoin join myKeywords on (_._2 === _.keywordId)
      } yield (values._1, dbAbs(values._3.getOrElse(0.5) - keywords.value))).groupBy(_._1)

    val globalQuerySum = normalize.map { case (articleId, line) =>
      (articleId, line.map(_._2).sum)
    }

    val sorted = globalQuerySum.sortBy(_._2.asc).take(50)

    val sortedArticles = for {
      (sv, article) <- sorted join articles on (_._1 === _.id)
    } yield article

    val list = Await.result(db.run(sortedArticles.result), Duration.Inf)

    Page(list, 1, 0, list.length, list.length)
  }

  def betaRecommend(userId: Option[Int]): Page[Article] = {
    val articleKeywords = TableQuery[ArticleKeywordsTable]
    val userKeywords = TableQuery[UserKeywordsTable]
    val dbAbs = SimpleFunction.unary[Double, Double]("abs")
    val oneDayAgo = new DateTime().minusDays(1)

    val freshArticles = articles.filter(_.publicationDate >= oneDayAgo)
    val myKeywords = userKeywords.filter(_.userId === userId)

    val freshArticlesKeywords = for {
      (article, keyword) <- freshArticles join articleKeywords on (_.id === _.articleId)
    } yield (article.id, keyword.keywordId, keyword.value)

    val interestingKeywords = freshArticlesKeywords.groupBy(_._2).map {
        case (keywordId, group) => keywordId
    }

    val myKeywordValues = for {
      (keyword, uk) <- interestingKeywords joinLeft myKeywords on (_ === _.keywordId)
    } yield (keyword, uk.map(_.value))

    val normalize = (
      for {
        (articles, keywords) <- freshArticlesKeywords join myKeywordValues on (_._2 === _._1)
    } yield (articles._1, dbAbs(articles._3 - keywords._2.getOrElse(0.5)))).groupBy(_._1)

    val globalQuerySum = normalize.map { case (articleId, line) =>
      (articleId, line.map(_._2).sum)
    }

    val sorted = globalQuerySum.sortBy(_._2.asc).take(50)

    Await.result(db.run(sorted.result), Duration.Inf).foreach(x => println((10 - x._2.get) * 10))

    val sortedArticles = for {
      (sv, article) <- sorted join articles on (_._1 === _.id)
    } yield article

    val list = Await.result(db.run(sortedArticles.result), Duration.Inf)

    Page(list, 1, 0, list.length, list.length)
  }

  def getPopularityIndex(articleId: Option[Int]): Double = {
    val articleKeywords = TableQuery[ArticleKeywordsTable]
    val keywordIds = articleKeywords.filter(_.articleId === articleId).map(_.keywordId)
    val values =
      for {
        (articleKeyword, article) <- articleKeywords join articles on (_.articleId === _.id)
        if articleKeyword.keywordId in keywordIds
        if article.publicationDate >= new DateTime().minusDays(3)
      } yield articleKeyword.value
    Await.result(db.run(values.sum.result), Duration.Inf).getOrElse(0)
  }
}
