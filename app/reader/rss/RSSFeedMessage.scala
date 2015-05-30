package reader.rss

case class RSSFeedMessage(
  guid: String,
  pubDate: String,
  title: String,
  description: String,
  link: String,
  author: String,
  category: String,
  comments: String,
  enclosure: String,
  source: String
)
