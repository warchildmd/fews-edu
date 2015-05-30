package reader.rss

case class RSSFeed(
  title: String,
  link: String,
  description: String,
  /* Optional */
  language: String,
  copyright: String,
  managingEditor: String,
  webMaster: String,
  pubDate: String,
  lastBuildDate: String,
  category: String,
  generator: String,
  docs: String,
  cloud: String,
  ttl: String,
  image: String,
  rating: String,
  textInput: String,
  skipHours: String,
  skipDays: String
  ) {
  var messages: Seq[RSSFeedMessage] = Seq()
}
