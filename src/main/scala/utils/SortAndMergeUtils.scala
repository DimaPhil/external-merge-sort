package utils

import java.io.File
import java.nio.file.Files

import net.liftweb.common.Loggable
import rws.{ReaderBuilder, Writer, WriterBuilder}

import scala.util.Try

object SortAndMergeUtils extends Loggable {

  def mergeSortedFiles[T](files: Array[File], tmpDir: File, i: Int = 0)(reduce: (T, T) => T)
                         (implicit readerBuilder: ReaderBuilder[T],
                          writerBuilder: WriterBuilder[T],
                          ord: Ordering[T]): Option[File] = {
    val filtered = files.filter(f => isFileSorted(f, readerBuilder)).map { f =>
      if (f.getName.startsWith("merge-tmp-")) {
        Files.move(f.toPath, new File(f.getParentFile, f.getName.replace("merge", s"merge-${System.currentTimeMillis()}")).toPath).toFile
      } else {
        f
      }
    }
    if (filtered.isEmpty) {
      None
    } else {
      val tmpFiles = filtered.grouped(32).zipWithIndex.map { case (fs, id) =>
        val readers = fs.map(readerBuilder.createReader)
        val result = mergeSortedIterators[T](readers.toList, tmpDir, s"merge-tmp-$i-$id")(reduce)
        readers.foreach(_.close())
        result
      }.toArray
      filtered.foreach(f => Try(f.delete()))
      if (tmpFiles.length <= 1) {
        tmpFiles.headOption
      } else {
        mergeSortedFiles(tmpFiles, tmpDir, i + 1)(reduce)
      }
    }
  }

  def isFileSorted[T](file: File, readerBuilder: ReaderBuilder[T])(implicit ord: Ordering[T]): Boolean = {
    try {
      val reader = readerBuilder.createReader(file)
      def go(prev: T, count: Int = 0): Boolean = {
        if (reader.hasNext && count < 50) {
          val head = reader.next()
          if (ord.compare(head, prev) >= 0) go(head, count + 1) else false
        } else {
          true
        }
      }
      val result = if (reader.hasNext) go(reader.next()) else true
      reader.close()
      result
    } catch {
      case t: Throwable =>
        logger.error(s"File ${file.getAbsolutePath} is broken:", t)
        false
    }
  }

  def mergeSortedIterators[T](iterators: List[Iterator[T]], dir: File, fileName: String)(reduce: (T, T) => T)
                             (implicit ord: Ordering[T], writerBuilder: WriterBuilder[T]): File = {
    def findMax(
                 all: List[BufferedIterator[T]],
                 accAll: List[BufferedIterator[T]] = Nil,
                 accMax: List[BufferedIterator[T]] = Nil,
                 current: Option[T] = None
               ): (List[BufferedIterator[T]], List[BufferedIterator[T]], T) = {
      all match {
        case Nil => (accAll, accMax, current.get)
        case h :: t =>
          if (current.isEmpty) {
            findMax(t, accAll, h :: accMax, Option(h.head))
          } else {
            val ordResult = ord.compare(h.head, current.get)
            if (ordResult < 0) {
              findMax(t, accMax.foldRight(accAll)(_ :: _), h :: Nil, Option(h.head))
            } else if (ordResult == 0) {
              findMax(t, accAll, h :: accMax, current.map(reduce(h.head, _)))
            } else {
              findMax(t, h :: accAll, accMax, current)
            }
          }
      }
    }

    def mergeDuplicates(rec: T, it: BufferedIterator[T]): T  = {
      it.next()
      if (it.hasNext && ord.compare(it.head, rec) == 0)
        mergeDuplicates(reduce(rec, it.head), it)
      else rec
    }

    def go(its: List[BufferedIterator[T]], writer: Writer[T]): Unit = {
      if (its.nonEmpty) {
        val (all, max, record) = findMax(its)
        val resultRecord = max.foldLeft(record)(mergeDuplicates)
        writer.write(resultRecord)
        val maxIterators = max.filter(_.hasNext)
        go(maxIterators ::: all, writer)
      }
    }

    val writer = writerBuilder.createWriter(dir, fileName)
    val buffered = iterators.filter(_.hasNext).map(_.buffered)
    if (buffered.nonEmpty) go(buffered, writer)
    writer.close()
  }

  def sortAndMergeFiles[T](files: List[File])(reduce: (T, T) => T)
                          (implicit readerBuilder: ReaderBuilder[T], writerBuilder: WriterBuilder[T], ord: Ordering[T]): Option[File] = {
    val memory = math.min(Runtime.getRuntime.freeMemory() / 8, 10000000).toInt
    val sortedFiles = files.flatMap { file =>
      val reader = readerBuilder.createReader(file)
      reader.grouped(memory).zipWithIndex.map { case (seq, id) =>
        val sorted = seq.sorted(ord)
        val writer = writerBuilder.createWriter(file.getParentFile, s"${file.getName}-sort-$id")
        sorted.foreach(writer.write)
        writer.close()
      }
    }
    mergeSortedFiles[T](sortedFiles.toArray, files.head.getParentFile)(reduce)
  }
}
