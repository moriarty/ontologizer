#
# A simple makefile to conduct the benchmark
#

.PHONY: all
all:
	cd ../ontologizer.parent && mvn package -DskipTests
	ONTOLOGIZER_ENABLE_EXPERIMENTAL=yes java -jar target/ontologizer.benchmark-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
		--obo ../ontologizer.tests/data/gene_ontology.1_2.obo.gz \
		--association ../ontologizer.tests/data/gene_association.sgd.gz \
		--output-dir /tmp/ontobench