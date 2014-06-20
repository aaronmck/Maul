import java.io.PrintWriter
import scala.util._

// generate some fake data to use with the read barcode splitter
val barcode1a = "AAAAAAA" // a very bad barcode
val barcode1b = "TTTTTTT" // ...again

val barcode2a = "GGGGGGG" // a very bad barcode
val barcode2b = "CCCCCCC" // ...again

// our output files
val read1Out = new PrintWriter("fakeFq1.fq")
val read2Out = new PrintWriter("fakeFq2.fq")
val read3Out = new PrintWriter("fakeFq3.fq")
val read4Out = new PrintWriter("fakeFq4.fq")

val numReads = 100
for (i <- 0 until numReads) {
  // fake reads
  writeFastqRecord(i,"NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN",1,read1Out)
  writeFastqRecord(i,"NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN",4,read4Out)
  if (i >= 20) {
    writeFastqRecord(i,generateErrors(barcode1a),2,read2Out)
    writeFastqRecord(i,generateErrors(barcode2a),3,read3Out)
  } else {
    writeFastqRecord(i,generateErrors(barcode1b),2,read2Out)
    writeFastqRecord(i,generateErrors(barcode2b),3,read3Out)
  }
}

read1Out.close
read2Out.close
read3Out.close
read4Out.close


def generateErrors(read: String, errorRate: Double = 0.1, rando : Random = new Random()): String =
  if (rando.nextDouble() <= (errorRate * read.length))
    createError(read)
  else
    read

def createError(read: String, rando : Random = new Random()) : String = {
  val pos = rando.nextInt(read.length-1)
  read.substring(0,pos) + switchBase(read(pos)).toString + read.substring(pos+1,read.length)
}

def switchBase(ch: Char) = ch match {
  case 'A' => 'C'
  case 'C' => 'G'
  case 'G' => 'T'
  case 'T' => 'A'
}

def writeFastqRecord(cnt: Int, readString: String, readNum: Int, writer: PrintWriter, qual: Int = 40, strand: String = "+") {
  writer.write("readNumber" + cnt + "/" + readNum + "\n")
  writer.write(readString + "\n")
  writer.write(strand + "\n")
  writer.write(readString.map{case(ch) => (qual + 33).toChar}.mkString("") + "\n")
}