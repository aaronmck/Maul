java -jar /net/shendure/vol10/aaronmck/source/sandbox/aaron/projects/PrePath/target/scala-2.10/prepath_2.10-1.0-one-jar.jar \
--inFQ1 /net/gs/vol1/home/aaronmck/shendure/data/nobackup/spiny/sequencing_data/Feb_13th_miseq/raw/s_1_1.fq.gz \
--inFQ2 /net/gs/vol1/home/aaronmck/shendure/data/nobackup/spiny/sequencing_data/Feb_13th_miseq/raw/s_1_3.fq.gz \
--inBarcodeFQ1 /net/gs/vol1/home/aaronmck/shendure/data/nobackup/spiny/sequencing_data/Feb_13th_miseq/raw/s_1_2.fq.gz \
--outFQ1 fastq1.fq.gz \
--outFQ2 fastq2.fq.gz \
--barcodes1 GCATAACT,CTCTGATT,CGTACGTA \
--barcodeStatsFile barcodeStats.txt \
--barcodeStatsFileUnknown barcodeStatsUknown.txt
