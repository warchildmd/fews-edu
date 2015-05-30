package reader.extractor

import java.io.File
import java.util
import java.util.regex.{Matcher, Pattern}
import java.util.{Collections, Comparator}

import play.api.Play._
import reader.stemmer.SnowballStemmer

import scala.collection.mutable.ListBuffer

/**
 * Created by Mihail on 4/21/2015.
 */
class JMatsuoWorker {

  def extract(document: String): Seq[String] = {
    getKeywords(document).map((keyword: ExtractedKeyword) => {
      keyword.text
    })
  }

  private def loadStopWords() = {
    scala.io.Source.
      fromFile(new File(current.path + "/app/reader/stopwords/romanian/stopwords.txt")).
      mkString.
      split("\\W+").toList
  }

  private def tokenizeStringRe(text: String, n: Int): util.ArrayList[String] = {
    val result: util.ArrayList[String] = new util.ArrayList[String]
    try {
      val stemClass: Class[_] = Class.forName("reader.stemmer.ext.romanianStemmer")
      val stemmer: SnowballStemmer = stemClass.newInstance.asInstanceOf[SnowballStemmer]
      var p: Pattern = Pattern.compile("([a-z]+)")
      if (n == 2) {
        p = Pattern.compile("(?=(\\b[a-z]+\\b \\b[a-z]+\\b))")
      }
      if (n == 3) {
        p = Pattern.compile("(?=(\\b[a-z]+\\b \\b[a-z]+\\b \\b[a-z]+\\b))")
      }
      val m: Matcher = p.matcher(text.toLowerCase)
      while (m.find) {
        val temporary: String = m.group(1)
        val words: Array[String] = temporary.split(" ")
        val first: String = words(0)
        val last: String = words(words.length - 1)
        if (!(stopwords.contains(first) || stopwords.contains(last))) {
          var stemmed_temp: String = ""
          for (www <- words) {
            stemmer.setCurrent(www)
            stemmer.stem
            val wwwStemmed = stemmer.getCurrent
            stemmed_temp += wwwStemmed + " "
          }
          stemmed_temp = stemmed_temp.trim
          result.add(stemmed_temp)
        }
      }
    }
    catch {
      case t: Throwable => {
        t.printStackTrace()
      }
    }
    result
  }

  private def getCoOq(textParam: String): util.HashMap[String, util.HashMap[String, Integer]] = {
    val freq: util.HashMap[String, util.HashMap[String, Integer]] = new util.HashMap[String, util.HashMap[String, Integer]]
    val text = textParam.toLowerCase
    val sentences: Array[String] = text.split("\\.")
    for (s <- sentences) {
      val alsWords: util.ArrayList[String] = this.tokenizeStringRe(s, 1)
      alsWords.addAll(this.tokenizeStringRe(s, 2))
      if (alsWords.size > 1) {
        var i: Int = 0
        while (i < alsWords.size - 1) {
          {
            {
              var j: Int = i + 1
              while (j < alsWords.size) {
                {
                  val iWord: String = alsWords.get(i)
                  val jWord: String = alsWords.get(j)
                  if (freq.containsKey(iWord)) {
                    val mainMap: util.HashMap[String, Integer] = freq.get(iWord)
                    if (mainMap.containsKey(jWord)) {
                      mainMap.put(jWord, mainMap.get(jWord) + 1)
                    }
                    else {
                      mainMap.put(jWord, 1)
                    }
                  }
                  else {
                    val thm: util.HashMap[String, Integer] = new util.HashMap[String, Integer]
                    thm.put(jWord, 1)
                    freq.put(iWord, thm)
                  }
                  if (freq.containsKey(jWord)) {
                    val mainMap: util.HashMap[String, Integer] = freq.get(jWord)
                    if (mainMap.containsKey(iWord)) {
                      mainMap.put(iWord, mainMap.get(iWord) + 1)
                    }
                    else {
                      mainMap.put(iWord, 1)
                    }
                  }
                  else {
                    val thm: util.HashMap[String, Integer] = new util.HashMap[String, Integer]
                    thm.put(iWord, 1)
                    freq.put(jWord, thm)
                  }
                }
                {
                  j += 1;
                  j - 1
                }
              }
            }
          }
          {
            i += 1;
            i - 1
          }
        }
      }
      alsWords.clear()
    }
    freq
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

  def extractHelper(text: String): Seq[String] = {
    val tokens = tokenize(text, 1) ++ tokenize(text, 2) ++ tokenize(text, 3)
    val sentences = text.split("(?<!\\w\\.\\w.)(?<![A-Z][a-z]\\.)(?<=\\.|\\?)\\s")
    val frequencies = tokens.groupBy(l => l).map(t => (t._1, t._2.length))
    val pairs = ListBuffer[(String, String)]()
    sentences.foreach((sentence: String) => {
      val sentenceTokens = (tokenize(sentence, 1) ++ tokenize(sentence, 2) ++ tokenize(sentence, 3)).sorted
      for (i <- sentenceTokens.indices; j <- i until sentenceTokens.size) {
        pairs.append((sentenceTokens(i), sentenceTokens(j)))
      }
    })
    val cofrequencies = pairs.groupBy(l => l).map(t => (t._1, t._2.length))
    Seq()
  }

  def getKeywords(text: String): Seq[ExtractedKeyword] = {
    val start_time: Long = System.currentTimeMillis
    totalWords = 0
    freqMap.clear
    var grams: util.ArrayList[String] = null
    grams = this.tokenizeStringRe(text, 1)
    import scala.collection.JavaConversions._
    for (temp <- grams) {
      if (freqMap.containsKey(temp)) {
        freqMap.put(temp, freqMap.get(temp) + 1)
      }
      else {
        totalWords += 1
        freqMap.put(temp, 1)
      }
    }
    grams = this.tokenizeStringRe(text, 2)
    import scala.collection.JavaConversions._
    for (temp <- grams) {
      if (freqMap.containsKey(temp)) {
        freqMap.put(temp, freqMap.get(temp) + 1)
      }
      else {
        freqMap.put(temp, 1)
      }
    }
    coFreqMap = getCoOq(text)
    val resultSet: util.ArrayList[ExtractedKeyword] = new util.ArrayList[ExtractedKeyword]
    val words: util.Set[String] = coFreqMap.keySet
    import scala.collection.JavaConversions._
    for (s <- words) {
      var result: Double = 0.0
      if (coFreqMap.containsKey(s)) {
        val C: util.Set[String] = coFreqMap.get(s).keySet
        val pc: Double = (freqMap.get(s) + 1.0) / (totalWords + words.size)
        var nw: Double = 0.0
        import scala.collection.JavaConversions._
        for (ts <- coFreqMap.get(s).keySet) {
          nw += coFreqMap.get(s).get(ts)
        }
        var max: Double = 0.0
        import scala.collection.JavaConversions._
        for (ts <- C) {
          val term: Double = ((((coFreqMap.get(s).get(ts) * 1.0) / nw) - nw * pc) * (((coFreqMap.get(s).get(ts) * 1.0) / nw) - (nw * pc))) / (nw * pc)
          if (term > max) {
            max = term
          }
          result += term
        }
        result -= max
        val keyw: ExtractedKeyword = new ExtractedKeyword(s, result)
        resultSet.add(keyw)
      }
    }
    Collections.sort(resultSet, new Comparator[ExtractedKeyword] {
      def compare(o1: ExtractedKeyword, o2: ExtractedKeyword): Int = {
        o1.value.compareTo(o2.value)
      }
    })
    val results: util.ArrayList[ExtractedKeyword] = new util.ArrayList[ExtractedKeyword]
    var sum: Double = 0.0;
    {
      var kk: Int = Math.max(0, resultSet.size - 10)
      while (kk < resultSet.size) {
        {
          results.add(resultSet.get(kk))
          sum += resultSet.get(kk).value.floatValue
        }
        {
          kk += 1
          kk - 1
        }
      }
    }
    {
      var i: Int = 0
      while (i < results.size) {
        {
          results.set(i, results.get(i).copy(value = results.get(i).value / sum))
        }
        {
          i += 1
          i - 1
        }
      }
    }
    import scala.collection.JavaConversions._
    results.toSeq
  }

  private val freqMap: util.HashMap[String, Integer] = new util.HashMap[String, Integer]
  private var coFreqMap: util.HashMap[String, util.HashMap[String, Integer]] = new util.HashMap[String, util.HashMap[String, Integer]]
  private var totalWords: Integer = 0
  private val stopwords: List[String] = loadStopWords()
  private val rulesFolder: String = "english"
}
