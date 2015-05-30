package reader

import java.util.concurrent.{TimeUnit, ThreadPoolExecutor, LinkedBlockingQueue}
import javax.inject.Inject
import models._
import org.joda.time.DateTime
import play.api.db.slick._
import play.api.inject.guice.GuiceInjectorBuilder
import reader.concurrency.{ThreadWorker, ExecutorMonitor}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class Reader @Inject()(val crawlSessionsRepo: CrawlSessions,
                       val feedsRepo: Feeds,
                       val keywordsRepo: Keywords,
                       val akRepo: ArticleKeywords,
                       val articlesRepo: Articles) {

  def crawl(): Unit = {
    val crawlSession = CrawlSession(None, new DateTime(), None)
    val crawlSessionId = crawlSessionsRepo.insert(crawlSession)

    val worksQueue: LinkedBlockingQueue[Runnable] = new LinkedBlockingQueue[Runnable]
    val pool: ThreadPoolExecutor = new ThreadPoolExecutor(100, 100, 10,
      TimeUnit.SECONDS, worksQueue)
    pool.allowCoreThreadTimeOut(true)

    val feeds = Await.result(feedsRepo.all(), Duration.Inf)

    feeds.foreach((feed: Feed) => {
      pool.execute(new ThreadWorker(articlesRepo, keywordsRepo, akRepo)(Await.result(crawlSessionId, Duration.Inf), feed))
    })

    // Starting the monitor thread as a daemon
    val monitor: Thread = new Thread(new ExecutorMonitor(pool))
    monitor.setDaemon(true)
    monitor.start()

    while (!pool.isTerminated) {
      Thread.sleep(1000)
    }

    crawlSessionsRepo.update(Await.result(crawlSessionId, Duration.Inf), crawlSession.copy(endTime = Some(new DateTime())))
  }

}
