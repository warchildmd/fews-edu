package controllers

import javax.inject.Inject

import play.api.mvc._
import reader.Reader

class Application @Inject()(protected val articlesRepo: models.Articles,
                            protected val akRepo: models.ArticleKeywords,
                            protected val crawlSessionsRepo: models.CrawlSessions,
                            protected val feedsRepo: models.Feeds,
                            protected val keywordsRepo: models.Keywords) extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def run = Action {
    val reader = new Reader(crawlSessionsRepo, feedsRepo, keywordsRepo, akRepo, articlesRepo)
    reader.crawl()
    Ok("RUN")
  }

}
