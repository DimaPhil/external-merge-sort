package rws

import java.io.{BufferedWriter, File, FileWriter}

import scala.util.Try

trait Writer[T] {
  def write(obj: T): Unit
  def close(): File
}

trait WriterBuilder[T] {
  def createWriter(dir: File, fileName: String): Writer[T] = {
    createWriter(new File(dir, s"$fileName.$fileExtension"))
  }
  def fileExtension: String
  protected def createWriter(file: File): Writer[T]
}

trait TextSerializer[T] {
  def serialize(obj: T): String
  def deserialize(line: String): Try[T]
}

class TextWriter[T](file: File, serializer: TextSerializer[T]) extends Writer[T] {

  private lazy val writer = new BufferedWriter(new FileWriter(file))
  private var empty = true

  override def write(obj: T): Unit = {
    this.synchronized{
      if (empty) {
        empty = false
        writer.write(serializer.serialize(obj))
      } else {
        writer.write("\n" + serializer.serialize(obj))
      }
    }
  }

  override def close(): File = {
    this.synchronized{
      writer.flush()
      writer.close()
      if (empty && !file.exists()) {
        file.createNewFile()
        file
      } else {
        file
      }
    }
  }

}