package rws

import java.io.File

import scala.util.Try

object StringRWS {
  object StringSerializer extends TextSerializer[String] {
    override def serialize(s: String): String = s
    override def deserialize(line: String): Try[String] = Try { line }
  }

  object StringReaderBuilder extends ReaderBuilder[String] {
    override def createReader(file: File): Reader[String] = {
      new TextReader[String](file, StringSerializer)
    }
  }

  object StringWriterBuilder extends WriterBuilder[String] {
    override def fileExtension = "txt"
    override def createWriter(file: File): Writer[String] = {
      new TextWriter[String](file, StringSerializer)
    }
  }
}

object IntRWS {
  object IntSerializer extends TextSerializer[Integer] {
    override def serialize(s: Integer): String = s.toString
    override def deserialize(line: String): Try[Integer] = Try { line.toInt }
  }

  object IntReaderBuilder extends ReaderBuilder[Integer] {
    override def createReader(file: File): Reader[Integer] = {
      new TextReader[Integer](file, IntSerializer)
    }
  }

  object IntWriterBuilder extends WriterBuilder[Integer] {
    override def fileExtension = "txt"
    override def createWriter(file: File): Writer[Integer] = {
      new TextWriter[Integer](file, IntSerializer)
    }
  }
}
