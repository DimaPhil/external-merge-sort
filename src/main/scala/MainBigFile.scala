import java.io.{File, PrintWriter}

import rws.IntRWS.{IntReaderBuilder, IntWriterBuilder}
import utils.SortAndMergeUtils

object MainBigFile {
  private object OrderingInteger extends Ordering[Integer] {
    override def compare(x: Integer, y: Integer): Int = Integer.compare(x, y)
  }

  private def sortInts(files: List[File]): Option[File] = {
    SortAndMergeUtils.sortAndMergeFiles[Integer](files)((a, _) => a)(IntReaderBuilder, IntWriterBuilder, OrderingInteger)
  }

  def main(args: Array[String]): Unit = {
    val startTime = System.currentTimeMillis()
    sortInts(new File("src/main/resourcesBig/all.txt") :: Nil)
    val endTime = System.currentTimeMillis()
    println("Took " + (endTime - startTime) + "ms to sort")
    println("Total time: " + (endTime - startTime))
  }
}
