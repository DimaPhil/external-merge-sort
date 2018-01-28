package rws

import java.io.{BufferedReader, File, FileReader}

import net.liftweb.common.Loggable

import scala.util.{Failure, Success, Try}

trait Reader[T] extends Iterator[T] {
  def close()
}

trait ReaderBuilder[T] {
  def createReader(file: File): Reader[T]
}

class TextReader[T >: Null](file: File, serializer: TextSerializer[T]) extends Reader[T] with Loggable {

  private val reader = new BufferedReader(new FileReader(file))
  private var _nextLine: T = _

  override def hasNext: Boolean = {
    if (_nextLine == null) {
      tryReadNext()
    }
    _nextLine != null
  }

  private def tryReadNext(tries: Int = 0): Boolean = {
    if (tries < 3) {
      read().getOrElse(tryReadNext(tries + 1))
    } else {
      read().get
    }
  }

  private def read(): Try[Boolean] = {
    val line = reader.readLine()
    if (line != null) {
      serializer.deserialize(line) match {
        case Success(s) =>
          _nextLine = s
          Success(true)
        case Failure(f) =>
          logger.error(s"Error in line '$line'")
          Failure(f)
      }
    } else {
      Success(false)
    }
  }

  override def next(): T = {
    if (_nextLine == null) {
      read() match {
        case Failure(t) => throw t
        case _ =>
      }
    }
    val line = _nextLine
    _nextLine = null
    line
  }

  override def close(): Unit = {
    reader.close()
  }

}
