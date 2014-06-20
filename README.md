A basic tool to strip off adapter sequences (assumes standard Illumina DNA adapters), merge reads that overlap, and seperate reads into seperate FASTQ files by barcode (hamming distance of 1 as default).  A basic command line would look like:

Building
==============
You need to have Java 1.7 and SBT installed.  Once you do, just type: 

`
sbt compile
`

to build the tool, and 

`
sbt one-jar
`

to make a single jar version with all of the dependencies built in

Running
===============
`
java -jar ./PrePath/target/scala-2.10/prepath_2.10-1.0-one-jar.jar 
--inFQ1 s_1_1.fq.gz 
--inFQ2 s_1_3.fq.gz 
--inBarcodeFQ1 s_1_2.fq.gz 
--outFQ ./output.test.base.name 
--barcodes CGATGT,TGACCA,CTTGTA,GTGAAA
`
