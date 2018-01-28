package generator

import java.io.PrintWriter

import scala.util.Random

object MaxGeneratorString {
  def randomString(length: Int): String = {
    Random.alphanumeric.take(length).mkString
  }

  def main(args: Array[String]): Unit = {
    for (filenum <- 0 until 10000) {
      if (filenum % 100 == 0) println("Processing " + filenum + "...")
      val filename = "src/main/resources/" + filenum.toString + ".txt"
      new PrintWriter(filename) {
        for (_ <- 0 until 10000) {
          val s = randomString(10)
          write(s + "\n")
        }
        close()
      }
    }
  }
}
