package generator

import java.io.PrintWriter

import scala.util.Random

object MaxGeneratorBigFile {
  def randomInt(): String = {
    Random.nextInt.toString
  }

  def main(args: Array[String]): Unit = {
    val filename = "src/main/resourcesBig/all.txt"
    new PrintWriter(filename) {
      for (_ <- 0 until 1000000000) {
        val s = randomInt()
        write(s + "\n")
      }
      close()
    }
  }
}
