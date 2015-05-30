package reader.extractor

import java.io.{File, FileNotFoundException}
import java.util._
import java.util.regex.{Matcher, Pattern}

import play.api.Play.current
import reader.stemmer.SnowballStemmer

class KExWorker {

  private def loadStopWords(file: String) {
    try {
      val br: Scanner = new Scanner(new File(file))
      while (br.hasNext) {
        stopWords.add(br.nextLine)
      }
    }
    catch {
      case e: FileNotFoundException => {
        e.printStackTrace
      }
    }
  }

  private def tokenizeStringRe(text: String, n: Int): ArrayList[String] = {
    val result: ArrayList[String] = new ArrayList[String]
    try {
      val stemClass: Class[_] = Class.forName("reader.stemmer.ext." + this.rulesFolder + "Stemmer")
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
        if (!(stopWords.contains(first) || stopWords.contains(last))) {
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
        t.printStackTrace
        // println(t.getMessage)
      }
    }
    return result
  }

  private def initialize {
    this.loadStopWords(current.path + "/app/reader/stopwords/" + this.rulesFolder + "/stopwords.txt")
  }

  private def getCoOq(textParam: String): HashMap[String, HashMap[String, Integer]] = {
    val freq: HashMap[String, HashMap[String, Integer]] = new HashMap[String, HashMap[String, Integer]]
    val text = textParam.toLowerCase
    val sentences: Array[String] = text.split("\\.")
    for (s <- sentences) {
      val alsWords: ArrayList[String] = this.tokenizeStringRe(s, 1)
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
                    val mainMap: HashMap[String, Integer] = freq.get(iWord)
                    if (mainMap.containsKey(jWord)) {
                      mainMap.put(jWord, mainMap.get(jWord) + 1)
                    }
                    else {
                      mainMap.put(jWord, 1)
                    }
                  }
                  else {
                    val thm: HashMap[String, Integer] = new HashMap[String, Integer]
                    thm.put(jWord, 1)
                    freq.put(iWord, thm)
                  }
                  if (freq.containsKey(jWord)) {
                    val mainMap: HashMap[String, Integer] = freq.get(jWord)
                    if (mainMap.containsKey(iWord)) {
                      mainMap.put(iWord, mainMap.get(iWord) + 1)
                    }
                    else {
                      mainMap.put(iWord, 1)
                    }
                  }
                  else {
                    val thm: HashMap[String, Integer] = new HashMap[String, Integer]
                    thm.put(iWord, 1)
                    freq.put(jWord, thm)
                  }
                }
                ({
                  j += 1; j - 1
                })
              }
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
      alsWords.clear
    }
    return freq
  }

  def this(rulesFolder: String) {
    this()
    this.rulesFolder = rulesFolder
    this.initialize
  }

  def getKeywords(text: String): Seq[ExtractedKeyword] = {
    val start_time: Long = System.currentTimeMillis
    totalWords = 0
    freqMap.clear
    var grams: ArrayList[String] = null
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
    val resultSet: ArrayList[ExtractedKeyword] = new ArrayList[ExtractedKeyword]
    val words: Set[String] = coFreqMap.keySet
    import scala.collection.JavaConversions._
    for (s <- words) {
      var result: Double = 0.0
      if (coFreqMap.containsKey(s)) {
        val C: Set[String] = coFreqMap.get(s).keySet
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
    val results: ArrayList[ExtractedKeyword] = new ArrayList[ExtractedKeyword]
    var sum: Double = 0.0;
    {
      var kk: Int = Math.max(0, resultSet.size - 10)
      while (kk < resultSet.size) {
        {
          results.add(resultSet.get(kk))
          sum += resultSet.get(kk).value.floatValue
        }
        ({
          kk += 1; kk - 1
        })
      }
    }
    {
      var i: Int = 0
      while (i < results.size) {
        {
          results.set(i, results.get(i).copy(value = results.get(i).value / sum))
        }
        ({
          i += 1; i - 1
        })
      }
    }
    import scala.collection.JavaConversions._
    return results.toSeq
  }

  private var freqMap: HashMap[String, Integer] = new HashMap[String, Integer]
  private var coFreqMap: HashMap[String, HashMap[String, Integer]] = new HashMap[String, HashMap[String, Integer]]
  private var totalWords: Integer = 0
  private var stopWords: Set[String] = new HashSet[String]
  private var rulesFolder: String = "english"
}
