#!/bin/sh

if [ x${LUCENE_JAR} == x ] ; then
   export LUCENE_JAR=../../lucene-core.jar
fi

javac -g -classpath ../../lucene-core.jar:../../lib/perst.jar *.java