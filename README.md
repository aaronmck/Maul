A basic tool to pull the specified barcoded reads from a set of FASTQ files.  Maul can handle single and dual barcoded runs.  The input is expected to be in either fastq or gziped fastq files (ending in .fq.gz).  The input files need to be separated into three or four files: 

- one fastq for the first sequenced read
- one fastq for the second read (optional)
- one or two fastqs respectively for single or dual barcoded libraries. 

Maul optionally outputs a file with read counts for each barcode combination, as well as a sorted histogram file for unknown barcodes that were seen during parsing.  The maximum edit distance can be set for barcodes allowing X number of mismatches.  Be careful with this parameter, as many barcode libraries are designed with an edit distance of 3 between individual barcodes, so setting this parameter above 1 could result in barcode switches. Set this to zero if you'd like to be more stringent about barcode parsing.

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

Running a single barcoded file
===============
```
java -jar <path_to_maul_jar>.jar \
--inFQ1 <reads1>.fq.gz \
--inFQ2 <reads2>.fq.gz \
--inBarcodeFQ1 <barcodereads1>.fq.gz \
--outFQ1 <outputreads1>.fq.gz \
--outFQ2 <outputreads2>.fq.gz \
--barcodes1 CGATGT,TGACCA,CTTGTA,GTGAAA \
--barcodeStatsFile statsFile.txt \
--barcodeStatsFileUnknown statsUnkownFile.txt
```

Running a single barcoded file
===============
```
java -jar <path_to_maul_jar>.jar \
--inFQ1 <reads1>.fq.gz \
--inFQ2 <reads2>.fq.gz \
--inBarcodeFQ1 <barcodereads1>.fq.gz \
--inBarcodeFQ2 <barcodereads2>.fq.gz \
--outFQ1 <outputreads1>.fq.gz \
--outFQ2 <outputreads2>.fq.gz \
--barcodes1 CGATGT,TGACCA,CTTGTA,GTGAAA \
--barcodes2 TGATGT,TTACCA,TTTGTA,TTGAAA \
--barcodeStatsFile statsFile.txt \
--barcodeStatsFileUnknown statsUnkownFile.txt
```
