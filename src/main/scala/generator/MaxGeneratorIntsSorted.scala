package generator

import java.io.PrintWriter

import scala.util.Random

object MaxGeneratorIntsSorted {
  def randomInt(): Int = {
    Random.nextInt
  }

  def main(args: Array[String]): Unit = {
    for (filenum <- 0 until 10000) {
      if (filenum % 100 == 0) println("Processing " + filenum + "...")
      val filename = "src/main/resources/" + filenum.toString + ".txt"
      new PrintWriter(filename) {
        val ints = new collection.mutable.ArrayBuffer[Int]()
        for (_ <- 0 until 10000) {
          val s = randomInt()
          ints += s
        }
        for (x <- ints.sortWith(_ < _)) {
          write(x.toString + "\n")
        }
        close()
      }
    }
  }
}
