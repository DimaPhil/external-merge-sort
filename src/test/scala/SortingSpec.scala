import java.io.{File, PrintWriter}

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import rws.IntRWS.{IntReaderBuilder, IntWriterBuilder}
import rws.StringRWS.{StringReaderBuilder, StringWriterBuilder}
import utils.SortAndMergeUtils

import scala.io.Source
import scala.util.Random

class SortingSpec extends Specification with Mockito {
  trait DataSetup extends Scope {
    val resourcesStringRoot = "src/main/resourcesString/"
    val resourcesIntRoot = "src/main/resourcesInt/"

    def randomString(length: Int): String = {
      Random.alphanumeric.take(length).mkString
    }

    def randomInt(): Int = {
      Random.nextInt()
    }
  }

  "SortAndMergeUtils" should {
    "sort and merge small files of strings" in new DataSetup {
      val maxIterations = 10000
      for (it <- 0 until maxIterations) {
        if (it % 100 == 0) println(s"Processing iteration ${it + 1}/$maxIterations")
        val files = new collection.mutable.ArrayBuffer[File]()
        val strings = new collection.mutable.ArrayBuffer[String]()
        for (i <- 0 until 100) {
          val filename = new File(resourcesStringRoot + i.toString + ".txt")
          files += filename
          new PrintWriter(filename) {
            val str: String = randomString(10)
            write(str + "\n")
            strings += str
            close()
          }
        }
        val fileOpt: Option[File] = SortAndMergeUtils.sortAndMergeFiles[String](files.toList)((a, _) => a)(StringReaderBuilder, StringWriterBuilder, Ordering.String)
        fileOpt should beSome
        val sortedStrings: List[String] = Source.fromFile(fileOpt.get).getLines().toList
        strings.sortWith(_ < _).toList.distinct must_== sortedStrings
        files.foreach(file => file.delete)
        fileOpt.get.delete
      }
    }

    "sort and merge small files of ints" in new DataSetup {
      object OrderingInteger extends Ordering[Integer] {
        override def compare(x: Integer, y: Integer): Int = Integer.compare(x, y)
      }

      val maxIterations = 10000
      for (it <- 0 until maxIterations) {
        if (it % 100 == 0) println(s"Processing iteration ${it + 1}/$maxIterations")
        val files = new collection.mutable.ArrayBuffer[File]()
        val ints = new collection.mutable.ArrayBuffer[Int]()
        for (i <- 0 until 100) {
          val filename = new File(resourcesIntRoot + i.toString + ".txt")
          files += filename
          new PrintWriter(filename) {
            val num: Int = randomInt()
            write(num.toString + "\n")
            ints += num
            close()
          }
        }
        val fileOpt: Option[File] = SortAndMergeUtils.sortAndMergeFiles[Integer](files.toList)((a, _) => a)(IntReaderBuilder, IntWriterBuilder, OrderingInteger)
        fileOpt should beSome
        val sortedInts: List[Int] = Source.fromFile(fileOpt.get).getLines().map(_.toInt).toList
        ints.sortWith(_ < _).toList.distinct must_== sortedInts
        files.foreach(file => file.delete)
        fileOpt.get.delete
      }
    }
  }
}
