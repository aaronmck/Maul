package main.scala.algorithms

/**
 * created by aaronmck on 6/27/14
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
package main.scala.algorithms

import algorithms.stats.OverlapCounts
import org.biojava3.core.sequence.DNASequence
import org.biojava3.core.sequence.compound.AmbiguityDNACompoundSet
import org.biojava3.alignment.{SubstitutionMatrixHelper, SimpleGapPenalty, Alignments}
import org.biojava3.alignment.Alignments.PairwiseSequenceAlignerType
import com.rockymadden.stringmetric.similarity.HammingMetric

/**
 * created by aaronmck on 2/14/14
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
case class ReadOverlapper(counter: OverlapCounts, minimumOverlap: Int = 10, maximumScore: Int = 1) {
  val gapPenality = new SimpleGapPenalty(8, 2)
  val matrix = SubstitutionMatrixHelper.getNuc4_4()

  def readDistance(string1: String, string2: String, minimumOverlap: Int, readLength: Int, maximumDivergence: Double = 0.08): Int = {
    var bestOverlap = -1
    var bestOverlapScore = Int.MaxValue
    var bestOverlapLength = Int.MaxValue

    // reverse complement the other read
    val str2RC = reverseComplementRead(string2)

    val windowSize = minimumOverlap
    for (i <- (string1.length - windowSize) to 0 by -1) {
      //println("window size = " + windowSize)
      val substr1 = string1.substring(i, string1.length)
      if (str2RC.length > windowSize) {
        val substr2 = str2RC.substring(0, math.min(substr1.length,str2RC.length))
        if (substr1.length > substr2.length) // we've run out of overlapping read
          return bestOverlapLength

        val dist = HammingMetric.compare( substr1.substring(0,substr2.length), substr2)

        val keeper = closeEnough(dist.getOrElse(Int.MaxValue),maximumDivergence,substr1.length)
        //println(substr1.substring(0,substr2.length) + " comp to \n" + substr2 + " with dist " + dist.getOrElse(Int.MaxValue) + " count it? " + keeper + " thresh = " + math.floor(maximumDivergence * substr2.length.toFloat))
        // find the longest overlap that's less than the minimum score
        if (keeper && dist.get < bestOverlapScore) {
          bestOverlapScore = dist.get
          bestOverlapLength = readLength - i
        }
      }
    }
    //println(string1 + "\n" + str2RC + "\n" + bestOverlapLength)
    return bestOverlapLength
  }

  def checkOverlap(read1: String, read2: String, barcode: String, forceTrim: Boolean, readLength: Int) = {//: Array[String] = {
    // setup the reads we have
    val dist = readDistance(read1, read2, minimumOverlap, readLength)

    //if (dist > read1.length && forceTrim)
    //  println("Unable to merge read with forced trim read 1 len " + read1.length + " read 2 " + + read2.length)

    // check that our alignment makes sense
    if (dist < read1.length) {
      counter.addRead(barcode, read1.length - dist, read1.length, read2.length)
    }
    else {
      counter.addRead(barcode, -1, read1.length, read2.length)
    }
    //return (Array[String](read1, read2))

  }

  def closeEnough(distance: Int, maximumDivergence: Double, length: Int): Boolean =
    if (math.floor(maximumDivergence * length.toFloat) >= distance) return true
    else return false

  def reverseBase(base: Char): Char = {
    if (base == 'A' || base == 'a') return 'T'
    else if (base == 'C' || base == 'c') return 'G'
    else if (base == 'G' || base == 'g') return 'C'
    else if (base == 'T' || base == 't') return 'A'
    else return base // we just return what they gave us
  }

  def reverseComplementRead(bases: String): String = bases.map(r => {reverseBase(r)}).reverse.mkString
}
