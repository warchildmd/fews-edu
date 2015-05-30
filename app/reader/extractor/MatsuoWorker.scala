package reader.extractor

import java.io.File
import java.util.regex.Pattern

import play.api.Play.current
import reader.stemmer.SnowballStemmer

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Created by Mihail on 4/21/2015.
 */
class MatsuoWorker {

  private def loadStopWords() = {
    scala.io.Source.
      fromFile(new File(current.path + "/app/reader/stopwords/romanian/stopwords.txt")).
      mkString.
      split("\\W+").toList
  }

  def tokenize(text: String, length: Integer): Seq[String] = {
    var result: Seq[String] = Seq()
    val stemClass: Class[_] = Class.forName("reader.stemmer.ext.romanianStemmer")
    val stemmer: SnowballStemmer = stemClass.newInstance.asInstanceOf[SnowballStemmer]

    var p: Pattern = Pattern.compile("([a-z]+)")
    if (length == 2) {
      p = Pattern.compile("(?=(\\b[a-z]+\\b \\b[a-z]+\\b))")
    }
    if (length == 3) {
      p = Pattern.compile("(?=(\\b[a-z]+\\b \\b[a-z]+\\b \\b[a-z]+\\b))")
    }
    val m = p.matcher(text.toLowerCase)
    while (m.find) {
      try {
        val temporary = m.group(1)
        val words = temporary.split(" ")
        val first = words(0)
        val last = words(words.length - 1)
        if (!(stopwords.contains(first) || stopwords.contains(last))) {
          val stemmed = words.map { word =>
            stemmer.setCurrent(word)
            stemmer.stem
            stemmer.getCurrent
          }.mkString(" ").trim
          result = result :+ stemmed
        }
      } catch {
        case e: Exception => { e.printStackTrace() }
      }
    }
    result
  }

  def freq(w: String, g: String): Int = {
    hash.get(w).get.getOrElse(g, 0)
  }

  def chi(w: String): Double = {
    var value = 0.0
    var maxValue = 0.0
    val nw = siblings.getOrElse(w, 0)
    selected.toList.foreach((g: String) => {
      val pg = siblings.getOrElse(g, 0) * 1.0 / tokens.length
      val current = (freq(w, g) - nw * pg) * (freq(w, g) - nw * pg) / nw * pg
      if (current > maxValue) maxValue = current
      value += current
    })
    value - maxValue
  }

  def extract(document: String): Seq[(String, Double)] = {
    val text  = document.toLowerCase
    tokens = tokenize(text, 1) ++ tokenize(text, 2) // ++ tokenize(document, 3)
    val sentences = text.split("(?<!\\w\\.\\w.)(?<![A-Z][a-z]\\.)(?<=\\.|\\?)\\s")
    frequencies = tokens.groupBy(l => l).map(t => (t._1, t._2.length))
    selected = frequencies.toList.sortBy(_._2).reverseMap((value) => value._1).take((frequencies.keys.size / 100.0 * 40.0).toInt)
    // val pairs = ListBuffer[(String, String)]()
    val insiblings = ListBuffer[String]()
    hash = mutable.HashMap[String, mutable.HashMap[String, Int]]()
    sentences.foreach((sentence: String) => {
      val sentenceTokens = tokenize(sentence, 1) ++ tokenize(sentence, 2) // ++ tokenize(sentence, 3)).sorted
      for (i <- sentenceTokens.indices; j <- i until sentenceTokens.size) {
        if (!hash.contains(sentenceTokens(i))) hash.put(sentenceTokens(i), mutable.HashMap[String, Int]())
        if (!hash.contains(sentenceTokens(j))) hash.put(sentenceTokens(j), mutable.HashMap[String, Int]())

        insiblings.append(sentenceTokens(i))
        insiblings.append(sentenceTokens(j))

        hash.get(sentenceTokens(i)).get.update(sentenceTokens(j), hash.get(sentenceTokens(i)).get.getOrElse(sentenceTokens(j), 0) + 1)
        hash.get(sentenceTokens(j)).get.update(sentenceTokens(i), hash.get(sentenceTokens(j)).get.getOrElse(sentenceTokens(i), 0) + 1)
      }
    })
    siblings = insiblings.groupBy(l => l).map(t => (t._1, t._2.length))

    selected.toList.map((w: String) => {
      (w, chi(w))
    }).sortBy(_._2).reverse.take(10)
  }

  def getKeywords(document: String): Seq[ExtractedKeyword] = {
    val text  = document.toLowerCase
    tokens = tokenize(text, 1) ++ tokenize(text, 2)
    val sentences = text.split("(?<!\\w\\.\\w.)(?<![A-Z][a-z]\\.)(?<=\\.|\\?)\\s")
    frequencies = tokens.groupBy(l => l).map(t => (t._1, t._2.length))
    selected = frequencies.toList.sortBy(_._2).reverseMap((value) => value._1).take((frequencies.keys.size / 100.0 * 40.0).toInt)
    val insiblings = ListBuffer[String]()
    hash = mutable.HashMap[String, mutable.HashMap[String, Int]]()
    sentences.foreach((sentence: String) => {
      val sentenceTokens = tokenize(sentence, 1) ++ tokenize(sentence, 2) // ++ tokenize(sentence, 3)).sorted
      for (i <- sentenceTokens.indices; j <- i until sentenceTokens.size) {
        if (!hash.contains(sentenceTokens(i))) hash.put(sentenceTokens(i), mutable.HashMap[String, Int]())
        if (!hash.contains(sentenceTokens(j))) hash.put(sentenceTokens(j), mutable.HashMap[String, Int]())

        insiblings.append(sentenceTokens(i))
        insiblings.append(sentenceTokens(j))

        hash.get(sentenceTokens(i)).get.update(sentenceTokens(j), hash.get(sentenceTokens(i)).get.getOrElse(sentenceTokens(j), 0) + 1)
        hash.get(sentenceTokens(j)).get.update(sentenceTokens(i), hash.get(sentenceTokens(j)).get.getOrElse(sentenceTokens(i), 0) + 1)
      }
    })
    siblings = insiblings.groupBy(l => l).map(t => (t._1, t._2.length))

    val keywords = selected.toList.map((w: String) => {
      (w, chi(w))
    }).sortBy(_._2).reverse.take(10).map(x => {
      ExtractedKeyword(x._1, x._2)
    })
    val sum = keywords.map(_.value).sum
    keywords.map((k: ExtractedKeyword) => {
      ExtractedKeyword(k.text, k.value / sum)
    })
  }

  private var frequencies: Map[String, Int] = null
  private var tokens: Seq[String] = null
  private var selected: Seq[String] = null
  private var siblings: Map[String, Int] = Map[String, Int]()
  private var hash: mutable.HashMap[String, mutable.HashMap[String, Int]] = null
  private val stopwords: List[String] = loadStopWords()
}
