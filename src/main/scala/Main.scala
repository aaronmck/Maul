package main.scala

import _root_.algorithms.BarcodeEditDistance
import _root_.algorithms.stats.OverlapCounts
import java.io.{PrintWriter, File, BufferedInputStream}
import net.sf.picard.fastq.{FastqWriter, FastqRecord, FastqWriterFactory, FastqReader}
import main.scala.input.{SequenceReader, ReadContainer, FastqSequenceReader}
import main.scala.output.FastqOutputManager
import java.util.logging.SimpleFormatter

import main.scala.stats._
import main.scala.algorithms.ReadCorrector
import java.util.logging.{Level, Logger}

/**
 * created by aaronmck on 2/13/14
 *
 * Copyright (c) 2014, aaronmck
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 *
 */
object Main extends App {
  val NOTAREALFILENAME = "/0192348102jr10234712930h8j19p0hjf129-348h512935"
  // please don't make a file with this name
  val NOTAREALFILE = new File(NOTAREALFILENAME)

  // parse the command line arguments
  val parser = new scopt.OptionParser[Config]("Maul") {
    head("Maul", "1.0")

    // *********************************** Inputs *******************************************************
    opt[File]("inFQ1") required() valueName ("<file>") action { (x, c) => c.copy(inFastq1 = x)} text ("out first end reads FASTQ")
    opt[File]("inFQ2") valueName ("<file>") action { (x, c) => c.copy(inFastq2 = x)} text ("the second end reads FASTQ")
    opt[File]("inBarcodeFQ1") valueName ("<file>") action { (x, c) => c.copy(inBarcodeFQ1 = x)} text ("The fastq file with the first set of barcodes")
    opt[File]("inBarcodeFQ2") valueName ("<file>") action { (x, c) => c.copy(inBarcodeFQ2 = x)} text ("The fastq file with the second set of barcodes")
    opt[File]("outFQ1") required() valueName ("<file>") action { (x, c) => c.copy(outFastq1 = x)} text ("the first output read fq")
    opt[File]("outFQ2") valueName ("<file>") action { (x, c) => c.copy(outFastq2 = x)} text ("the second output read fq")
    opt[File]("outFQIndex1") required() valueName ("<file>") action { (x, c) => c.copy(outFastqIndex1 = x)} text ("the first input output read fq")
    opt[File]("outFQIndex2") valueName ("<file>") action { (x, c) => c.copy(outFastqIndex2 = x)} text ("the second index output read fq")
    opt[File]("barcodeStatsFile") valueName ("<file>") action { (x, c) => c.copy(barcodeStatsFile = x)} text ("the output barcode stats file")
    opt[File]("barcodeStatsFileUnknown") valueName ("<file>") action { (x, c) => c.copy(barcodeStatsUknownFile = x)} text ("the output barcode stats file")

    opt[String]("barcodes1") action { (x, c) => c.copy(barcodes1 = x)} text ("the list of first index barcodes to look for, comma separated with no spaces. 'all' is accepted")
    opt[String]("barcodes2") action { (x, c) => c.copy(barcodes2 = x)} text ("the list of second index barcodes to look for, comma separated with no spaces. 'all' is accepted")

    opt[Boolean]("renameReadPairs") action { (x, c) => c.copy(renameReads = x)} text ("rename the first and second reads so that they end with slash 1 and slash 2 respectively")
    opt[Boolean]("checkReadNames") action { (x, c) => c.copy(checkReads = x)} text ("do we check that the read names for pairs are the same, except the ending slash number (default true)")
    opt[Int]("maxDistance") action { (x, c) => c.copy(maxBarcodeDist = x)} text ("the max edit distance we allow for a barcode (default 1)")
    opt[Int]("hexTrim") action { (x, c) => c.copy(hexTrim = x)} text ("if we want to trim off the first X bases since the reads were generated with random heximer primers")


    // some general command-line setup stuff
    note("Split the reads into output fastq files\n")
    help("help") text ("prints the usage information you see here")
  }

  // *********************************** Run *******************************************************
  // run the actual read processing -- our argument parser found all of the parameters it needed
  parser.parse(args, Config()) map {
    config => {
      // WHY IS setting up logging the worst part of this whole program? Still too much Java left in Scala...
      System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT]:%4$s:(%2$s): %5$s%n")
      val logger = Logger.getLogger("MainProcessing")
      logger.setLevel(Level.INFO)

      val perWhat = 1000000 // output status for every X reads -- the associated message text may have to change as well

      // are we using one fastq or two
      val pairedEnd = config.inFastq2.exists()

      // ----------------------------------------------------------------------------------------------------------------------------
      // setup the input files
      // ----------------------------------------------------------------------------------------------------------------------------
      val inputFQ1 = new FastqReader(config.inFastq1)
      val inputFQ2: Option[FastqReader] = if (pairedEnd) Some(new FastqReader(config.inFastq2)) else None

      val useBarcode1 = config.inBarcodeFQ1 != NOTAREALFILE && config.inBarcodeFQ1.exists // we setup this variable to use for barcode output
      var inBarcodeFQ1: Option[FastqReader] = None
      if (useBarcode1) inBarcodeFQ1 = Some(new FastqReader(config.inBarcodeFQ1))

      val useBarcode2 = config.inBarcodeFQ2 != NOTAREALFILE && config.inBarcodeFQ2.exists // we setup this variable to use for barcode output
      var inBarcodeFQ2: Option[FastqReader] = None
      if (useBarcode2) inBarcodeFQ2 = Some(new FastqReader(config.inBarcodeFQ2))

      // ----------------------------------------------------------------------------------------------------------------------------
      // setup the output files
      // ----------------------------------------------------------------------------------------------------------------------------
      val writerFact = new FastqWriterFactory()
      val outFQ1 = writerFact.newWriter(config.outFastq1)
      val outFQ2: Option[FastqWriter] = if (pairedEnd) Some(writerFact.newWriter(config.outFastq2)) else None

      val outIndexFQ1: Option[FastqWriter] = if (useBarcode1) Some(writerFact.newWriter(config.outFastqIndex1)) else None
      val outIndexFQ2: Option[FastqWriter] = if (useBarcode2) Some(writerFact.newWriter(config.outFastqIndex2)) else None

      // determine barcode parsing - are we just dumping all the barcoded reads out?
      val barcodes1 = BarcodeEditDistance.parseBarcodes(config.barcodes1)
      val barcodes2 = BarcodeEditDistance.parseBarcodes(config.barcodes2)

      // get a metrics output file
      val metricsOutput = BarcodeOccurance(barcodes1, barcodes2, config.maxBarcodeDist)
      val renamer = new ReadCorrector(config.renameReads,config.hexTrim)

      // ----------------------------------------------------------------------------------------------------------------------------
      // process the reads
      // ----------------------------------------------------------------------------------------------------------------------------

      var readCount = 0
      var readCountOutput = 0

      // time the process
      var startTime = System.nanoTime()
      val overlap = new Array[Int](1000)
      val calcOverlap = config.overlapFile != NOTAREALFILENAME

      // we assume there's paired reads in both files, though we check as we go
      while (inputFQ1.hasNext()) {
        val read1 = inputFQ1.next()
        val read2: Option[FastqRecord] = if (pairedEnd) Some(inputFQ2.get.next()) else None

        val barcode1: Option[FastqRecord] = if (useBarcode1) Some(inBarcodeFQ1.get.next()) else None
        val barcode2: Option[FastqRecord] = if (useBarcode2) Some(inBarcodeFQ2.get.next()) else None

        // get the renamed reads
        val renamedReads = renamer.correctReads(read1, read2.get)

        val barcodeAndDistance1 = if (barcodes1.isDefined) BarcodeEditDistance.distance(barcode1, barcodes1.get) else Tuple2[Option[String], Int](Some("all"), 0)
        val barcodeAndDistance2 = if (barcodes2.isDefined) BarcodeEditDistance.distance(barcode2, barcodes2.get) else Tuple2[Option[String], Int](Some("all"), 0)

        // should we rename the reads in any way?
        // -------------------------------------------------------------------------------------------------------------------------
        if (renamedReads.isDefined &&
          (!barcodes1.isDefined || (barcodeAndDistance1._1.isDefined && barcodeAndDistance1._2 <= config.maxBarcodeDist)) &&
          (!barcodes2.isDefined || (barcodeAndDistance2._1.isDefined && barcodeAndDistance2._2 <= config.maxBarcodeDist))) {
          readCountOutput += 1

          metricsOutput.addEditDistance(barcodeAndDistance1._1, barcodeAndDistance2._1, barcodeAndDistance1._2, barcodeAndDistance2._2)

          if (useBarcode1) outIndexFQ1.get.write(barcode1.get)
          if (useBarcode2) outIndexFQ2.get.write(barcode2.get)

          outFQ1.write(renamedReads.get._1)
          if (pairedEnd)
            outFQ2.get.write(renamedReads.get._2.get)
        }

        metricsOutput.addEditDistance(if (barcode1.isDefined) Some(barcode1.get.getReadString) else Some("all"),  if (barcode2.isDefined) Some(barcode2.get.getReadString) else Some("all"), 0, 0)

        readCount += 1

        // output status?
        if (readCount % perWhat == 0) {
          val endTime = System.nanoTime()

          logger.info(readCount / perWhat + " million processed; approximately " + ((endTime - startTime) / 1000000000.0) + " seconds per million reads: " + readCountOutput + " reads output")
          startTime = endTime
        }
      }

      logger.info("total reads = " + readCount)
      logger.info("total reads output = " + readCountOutput)

      try {
        if (config.barcodeStatsFile != NOTAREALFILENAME)
          metricsOutput.toFile(config.barcodeStatsFile)
        if (config.barcodeStatsUknownFile != NOTAREALFILENAME)
          metricsOutput.toUnknownFile(config.barcodeStatsUknownFile)
      } catch {
        case (e: Exception) => logger.severe("Unable to output the metrics file, Exception with string: " + e.getMessage)
      }

      outFQ1.close()
      if (useBarcode1) outIndexFQ1.get.close()
      if (useBarcode2) outIndexFQ2.get.close()

      if (pairedEnd) outFQ2.get.close()
    }
  } getOrElse {
    Console.println("Unable to parse the command line arguments you passed in, please check that your parameters are correct")
  }

  /**
   * reverse complement a String of nucleotides. Non ACGTs are ignored and kept the same
   * @param base the string of bases
   * @return the inverse string, all UPPERCASE
   */
  def reverseComp(base: String): String = {
    var strng = ""
    base.toUpperCase.reverse.foreach(i => {
      if (i == 'A') strng += 'T'
      else if (i == 'T') strng += 'A'
      else if (i == 'C') strng += 'G'
      else if (i == 'G') strng += 'C'
      else strng += i
    })
    return strng
  }


}

/*
 * the configuration class, it stores the user's arguments from the command line, set defaults here
 */
case class Config(inFastq1: File = new File(Main.NOTAREALFILENAME),
                  inFastq2: File = new File(Main.NOTAREALFILENAME),
                  inBarcodeFQ1: File = new File(Main.NOTAREALFILENAME),
                  inBarcodeFQ2: File = new File(Main.NOTAREALFILENAME),
                  outFastq1: File = new File(Main.NOTAREALFILENAME),
                  outFastq2: File = new File(Main.NOTAREALFILENAME),
                  outFastqIndex1: File = new File(Main.NOTAREALFILENAME),
                  outFastqIndex2: File = new File(Main.NOTAREALFILENAME),
                  outBarcodeFQ: File = new File(Main.NOTAREALFILENAME),
                  barcodeStatsFile: File = new File(Main.NOTAREALFILENAME),
                  barcodeStatsUknownFile: File = new File(Main.NOTAREALFILENAME),
                  barcodes1: String = "all",
                  barcodes2: String = "all",
                  renameReads: Boolean = false,
                  checkReads: Boolean = true,
                  maxBarcodeDist: Int = 1,
                  overlapFile: File = new File(Main.NOTAREALFILENAME),
                  readLength: Int = 101,
                  hexTrim: Int = 0
                   )

