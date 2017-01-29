package main.scala.input

import java.io.File
import net.sf.picard.fastq.{FastqWriter, FastqRecord, FastqWriterFactory, FastqReader}

/**
 * created by aaronmck on 2/15/14
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
case class FastqSequenceReader(inputFastq: File, firstRead: Boolean ) extends SequenceReader {
  val input = new FastqReader(inputFastq)

  def next() : ReadContainer = {
    val read = input.next()
    return ReadContainer(read.getReadHeader,read.getReadString,read.getBaseQualityString,Some(read.getBaseQualityHeader),None,None,true,firstRead)
  }

  def hasNext() : Boolean = input.hasNext

  override def iterator: Iterator[ReadContainer] = this
}

object FastqSequenceReader {
  def fastqToOutputString(record: FastqRecord): String = {
    record.getReadHeader + "\n" + record.getReadString()  + "\n+\n" +record.getBaseQualityString()  + "\n"
  }
}