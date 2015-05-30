package reader.concurrency

import javax.inject.Inject

import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import reader.Utils
import reader.extractor.{JMatsuoWorker, ExtractedKeyword, MatsuoWorker}
import reader.rss._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class ThreadWorker @Inject()(val articlesRepo: models.Articles,
                             val keywordsRepo: models.Keywords,
                             val akRepo: models.ArticleKeywords) (csId: Int, param: Feed) extends Runnable {
  val feed = param

  def run() {
    val rssFeed = RSSFeedParser readFeed feed.link
    var articlesSaved: Int = 0
    try {
      rssFeed.get.messages.foreach((message: RSSFeedMessage) => {
        try {
          /* BEGIN Initialize article */
          var article = fromRSSMessage(csId, feed.id.get, message)
          /* END Initialize article */
          val articleId = articlesRepo.insert(article)

          articlesSaved += 1
          val articleData = Utils.getArticleData(article)
          article = article.copy(content = Utils.removeAccents(articleData._1),
            image = Some(articleData._2))
          articlesRepo.update(Await.result(articleId, Duration.Inf), article)

          /* BEGIN Keyword Extraction */
          val articleContent = Utils.removeAccents(article.title + ". " +
            article.title + ". " + article.description + ". " +
            article.content)

          val k = new MatsuoWorker // new KExWorker("romanian")
          val keywords = k.getKeywords(articleContent)
          keywords.foreach((extractedKeyword: ExtractedKeyword) => {
            val keyword = Await.result(keywordsRepo.findByContent(extractedKeyword.text), Duration.Inf)
            if (keyword.isEmpty) {
              val newKeyword = Keyword(None, extractedKeyword.text, feed.languageId)
              val keywordId = keywordsRepo.insert(newKeyword)
              akRepo.insert(ArticleKeyword(None, Await.result(articleId, Duration.Inf),
                Await.result(keywordId, Duration.Inf), extractedKeyword.value))
            } else {
              val newKeyword = keyword.get
              val keywordId = newKeyword.id
              akRepo.insert(ArticleKeyword(None, Await.result(articleId, Duration.Inf), keywordId.get, extractedKeyword.value))
            }
          })
          /* END Keyword Extraction */
        } catch {
          case m: com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException => {
            // println("Article exists!")
          }
          case e: com.mysql.jdbc.MysqlDataTruncation => {
            println("Data too long")
          }
        }
      })
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  def fromRSSMessage(csId: Int, fId: Int, message: RSSFeedMessage): Article = {
    val title = Utils.removeAccents(message.title)
    val description = Utils.removeAccents(message.description)
    val content = ""
    val link = message.link
    val image = ""
    val feedId = fId
    val hash = Utils.computeHash(message.link)
    val crawlSessionId = csId
    val createdAt = new DateTime

    val sdf = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss z")
    val cdf = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z")
    var publicationDate: DateTime = null
    try {
      publicationDate = cdf.parseDateTime(message.pubDate)
    } catch {
      case e: Exception =>
        try {
          publicationDate = sdf.parseDateTime(message.pubDate)
        } catch {
          case e: Exception =>
            publicationDate = new DateTime
        }
    }
    Article(None, title, Some(description), content, link, Some(image), publicationDate,
      feedId, hash, crawlSessionId, createdAt)
  }
}
