import java.io.File

import rws.IntRWS.{IntReaderBuilder, IntWriterBuilder}
import rws.StringRWS.{StringReaderBuilder, StringWriterBuilder}
import utils.SortAndMergeUtils

object Main {
  private object OrderingInteger extends Ordering[Integer] {
    override def compare(x: Integer, y: Integer): Int = Integer.compare(x, y)
  }

  private def sortStrings(files: List[File], groupSize: Int): Option[File] = {
    SortAndMergeUtils.sortAndMergeFiles[String](files, groupSize)((a, _) => a)(StringReaderBuilder, StringWriterBuilder, Ordering.String)
  }

  private def sortInts(files: List[File], groupSize: Int): Option[File] = {
    SortAndMergeUtils.sortAndMergeFiles[Integer](files, groupSize)((a, _) => a)(IntReaderBuilder, IntWriterBuilder, OrderingInteger)
  }

  def main(args: Array[String]): Unit = {
    val files = if (args.length > 1)
      args.toList.map(filename => new File("src/main/resources/" + filename))
    else
      new File("src/main/" + args(0)).listFiles.filter(_.isFile).toList
    for (divisor <- 4 :: 8 :: 12 :: 16 :: 20 :: 32 :: Nil) {
      val groupSize = math.min(Runtime.getRuntime.freeMemory() / divisor, 10000000).toInt
      var startTime = System.currentTimeMillis()
      sortStrings(files, groupSize)
      var endTime = System.currentTimeMillis()
      println("Took " + (endTime - startTime) + "ms for strings")
      System.gc()
      startTime = System.currentTimeMillis()
      sortInts(files, groupSize)
      endTime = System.currentTimeMillis()
      println("Took " + (endTime - startTime) + "ms for ints")
      System.gc()
    }
  }
}
