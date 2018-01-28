package generator

import java.io.PrintWriter

import scala.util.Random

object MaxGeneratorInts {
  def randomInt(): String = {
    Random.nextInt.toString
  }

  def main(args: Array[String]): Unit = {
    for (filenum <- 0 until 10000) {
      if (filenum % 100 == 0) println("Processing " + filenum + "...")
      val filename = "src/main/resources/" + filenum.toString + ".txt"
      new PrintWriter(filename) {
        for (_ <- 0 until 10000) {
          val s = randomInt()
          write(s + "\n")
        }
        close()
      }
    }
  }
}
