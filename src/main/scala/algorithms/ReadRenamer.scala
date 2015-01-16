package main.scala.algorithms

import net.sf.picard.fastq.FastqRecord
import java.util.logging.{Level, Logger}

/**
 * created by aaronmck on 2/19/14
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

/**
 * rename reads so that they have the expected Illumina 1 -> 2 name endings.  This is required for some
 * tools in Picard, so we might as well do it now
 */
class ReadRenamer(emitMismatchedReads: Boolean = true, firstExt: String = "1",secondExt: String = "2") {
  val logger = Logger.getLogger("ReadRenamer")
  logger.setLevel(Level.INFO)

  var readNameVolations = 0

  def renameReads(readName1: String,readName2: String): Option[Tuple2[String,String]] = {
    val strippedRead1 = readName1.substring(0,readName1.length -1)
    val strippedRead2 = readName2.substring(0,readName2.length -1)

    // verify the read names are the same
    if (!strippedRead1.equals(strippedRead2)) {
      readNameVolations += 1
      logger.warning("VIOLATION : " + readName1 + " " + readName2)
      if (!emitMismatchedReads)
        return None
    }
    return Some(Tuple2(strippedRead1 + firstExt,strippedRead2 + secondExt))
  }

  def renamePicardReads(read1: FastqRecord, read2: FastqRecord): Option[Tuple2[FastqRecord,Option[FastqRecord]]] = {
    val names = renameReads(read1.getReadHeader,read2.getReadHeader)

    if (!names.isDefined) return None

    val newRead1 = new FastqRecord(names.get._1,read1.getReadString,read1.getBaseQualityHeader,read1.getBaseQualityString)
    val newRead2 = new FastqRecord(names.get._2,read2.getReadString,read2.getBaseQualityHeader,read2.getBaseQualityString)

    return(Some(Tuple2[FastqRecord,Option[FastqRecord]](newRead1,Some(newRead2))))
  }
}
