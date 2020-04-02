import java.io.File

import rws.IntRWS.{IntReaderBuilder, IntWriterBuilder}
import utils.SortAndMergeUtils

object MainBigFile {
  private object OrderingInteger extends Ordering[Integer] {
    override def compare(x: Integer, y: Integer): Int = Integer.compare(x, y)
  }

  private def sortInts(files: List[File], groupSize: Int): Option[File] = {
    SortAndMergeUtils.sortAndMergeFiles[Integer](files, groupSize)((a, _) => a)(IntReaderBuilder, IntWriterBuilder, OrderingInteger)
  }

  def main(args: Array[String]): Unit = {
    val startTime = System.currentTimeMillis()
    val groupSize = math.min(Runtime.getRuntime.freeMemory() / 8, 10000000).toInt
    sortInts(new File("src/main/resourcesBig/all.txt") :: Nil, groupSize)
    val endTime = System.currentTimeMillis()
    println("Took " + (endTime - startTime) + "ms to sort")
    println("Total time: " + (endTime - startTime))
  }
}
