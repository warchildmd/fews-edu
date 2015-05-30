package reader.rss

import scala.language.postfixOps
import scala.xml.XML

object RSSFeedParser {

  def readFeed(feedUrl: String): Option[RSSFeed] = {
    var feed: Option[RSSFeed] = None

    val xml = XML load feedUrl
    val channel = xml \ "channel"

    feed = Option(RSSFeed((channel \ "title") text,
      (channel \ "link") text,
      (channel \ "description") text,
      (channel \ "language") text,
      (channel \ "copyright") text,
      (channel \ "managingEditor") text,
      (channel \ "webMaster") text,
      (channel \ "pubDate") text,
      (channel \ "lastBuildDate") text,
      (channel \ "category") text,
      (channel \ "generator") text,
      (channel \ "docs") text,
      (channel \ "cloud") text,
      (channel \ "ttl") text,
      (channel \ "image") text,
      (channel \ "rating") text,
      (channel \ "textInput") text,
      (channel \ "skipHours") text,
      (channel \ "skipDays") text))

    feed.get.messages = (channel \ "item") map { item =>
      RSSFeedMessage((item \ "guid") text,
        (item \ "pubDate") text,
        (item \ "title") text,
        (item \ "description") text,
        (item \ "link") text,
        (item \ "author") text,
        (item \ "category") text,
        (item \ "comments") text,
        (item \ "enclosure") text,
        (item \ "source") text)
    }

    return feed
  }
}
