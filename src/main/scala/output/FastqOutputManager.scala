package main.scala.output

import net.sf.picard.fastq.{FastqRecord, FastqWriter, FastqWriterFactory}
import main.scala.input.ReadContainer
import java.util
import scala.collection.mutable
import java.io.File

/**
 * created by aaronmck on 2/16/14
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
class FastqOutputManager(basePath: File, allReadsInOutOuput: Boolean = true) {
  // open the output files
  val writerFact = new FastqWriterFactory()
  val outputWriters = new mutable.HashMap[String, FastqWriter]()

  def addRead(read: ReadContainer) {
    var merged = "all"

    // if we're not using the all approach, setup a barcode naming output
    if (!allReadsInOutOuput)
      merged = read.barcode1.getOrElse("UNSET")

    if (read.paired)
      if (read.firstOfPair)
        merged += "." + "1"
      else
        merged += "." + "2"

    if (!(outputWriters contains merged))
      outputWriters(merged) = writerFact.newWriter(new File(basePath.getAbsolutePath + File.separator  + merged + ".fq.gz"))

    val fastqRecord = new FastqRecord(read.readName, read.sequence, read.qualitiesHeader.getOrElse("UNKNOWN"), read.qualities)
    outputWriters(merged).write(fastqRecord)
  }

  def closeAll() = outputWriters.map{case(_,writer) => writer.close()}

}
