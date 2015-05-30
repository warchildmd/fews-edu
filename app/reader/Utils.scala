package reader

import java.security.MessageDigest
import java.text.Normalizer

import de.l3s.boilerpipe.extractors.ArticleExtractor
import models.Article
import org.jsoup.Jsoup

/**
 * Created by Mihail on 5/29/2015.
 */
object Utils {
  def getArticleData(article: Article): (String, String) = {
    var content = ""
    var image = ""
    try {
      val document = Jsoup.connect(article.link)
        .timeout(10000)
        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
        .referrer("http://www.google.com").get
      content = ArticleExtractor.INSTANCE.getText(document.html)
      val refresh = document.head.select("meta[property=og:image]")
      if (!refresh.isEmpty) {
        val element = refresh.get(0)
        image = element.attr("content")
      }
    } catch {
      case e: Exception => println (e.getMessage)
    }
    (content, image)
  }

  def removeAccents(text: String) = {
    Normalizer.normalize(text, Normalizer.Form.NFD)
      .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
  }

  def computeHash(input: String) = {
    val key = input.getBytes
    try {
      val md = MessageDigest.getInstance("SHA-1")
      val hash = md.digest(key)

      var result = ""
      hash.foreach((b: Byte) => {
        result += (b + 256).toHexString
      })
      result
    } catch {
      case e: Exception => {
        e.printStackTrace()
        null
      }
    }
  }
}
