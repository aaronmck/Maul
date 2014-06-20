package test.scala.stats

import algorithms.BarcodeEditDistance
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import main.scala.stats.BarcodeOccurance

/**
 * created by aaronmck on 6/19/14
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
class BarcodeOccuranceTest extends FlatSpec with ShouldMatchers {
  "BarcodeEditDistance" should "store an edit distance entry correctly" in {
    val barcodes1 = Some(Array[String]("TTTT","TAAA"))
    val barcodes2 = None
    val barcodeOccurance = new BarcodeOccurance(barcodes1,barcodes2,1)

    barcodeOccurance.addEditDistance(Some("TTTT"),None,0,0)
    val counts = barcodeOccurance.count(Some("TTTT"),None).get
    assert(counts.length == 3)
    assert(counts(0) == 1)
    assert(counts(1) == 0)
    assert(counts(2) == 0)
  }

  "BarcodeEditDistance" should "store errors correctly" in {
    val barcodes1 = Some(Array[String]("TTTT","TAAA"))
    val barcodes2 = None
    val barcodeOccurance = new BarcodeOccurance(barcodes1,barcodes2,1)

    barcodeOccurance.addEditDistance(Some("TTTT"),None,1,0)
    val counts = barcodeOccurance.count(Some("TTTT"),None).get
    assert(counts.length == 3)
    assert(counts(0) == 0)
    assert(counts(1) == 1)
    assert(counts(2) == 0)
  }

  "BarcodeEditDistance" should "handle incorrect barcodes without crashing" in {
    val barcodes1 = Some(Array[String]("TTTT","TAAA"))
    val barcodes2 = None
    val barcodeOccurance = new BarcodeOccurance(barcodes1,barcodes2,1)

    barcodeOccurance.addEditDistance(Some("AASDA"),None,1,0)
    val counts = barcodeOccurance.count(Some("TTTT"),None).get
    assert(counts.length == 3)
    assert(counts(0) == 0)
    assert(counts(1) == 0)
    assert(counts(2) == 0)
  }
}