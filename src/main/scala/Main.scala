import java.io.File

import rws.IntRWS.{IntReaderBuilder, IntWriterBuilder}
import rws.StringRWS.{StringReaderBuilder, StringWriterBuilder}
import utils.SortAndMergeUtils

object Main {
  private object OrderingInteger extends Ordering[Integer] {
    override def compare(x: Integer, y: Integer): Int = Integer.compare(x, y)
  }

  private def sortStrings(files: List[File]): Option[File] = {
    SortAndMergeUtils.sortAndMergeFiles[String](files)((a, _) => a)(StringReaderBuilder, StringWriterBuilder, Ordering.String)
  }

  private def sortInts(files: List[File]): Option[File] = {
    SortAndMergeUtils.sortAndMergeFiles[Integer](files)((a, _) => a)(IntReaderBuilder, IntWriterBuilder, OrderingInteger)
  }

  def main(args: Array[String]): Unit = {
    val files = if (args.length > 1)
      args.toList.map(filename => new File("src/main/resources/" + filename))
    else
      new File("src/main/" + args(0)).listFiles.filter(_.isFile).toList
    var startTime = System.currentTimeMillis()
    sortStrings(files)
    var endTime = System.currentTimeMillis()
    println("Took " + (endTime - startTime) + "ms for strings")
    startTime = System.currentTimeMillis()
    sortInts(files)
    endTime = System.currentTimeMillis()
    println("Took " + (endTime - startTime) + "ms for ints")
  }
}
