#!/bin/bash

# Set the source and output directories
SRC_DIR="src/main/java"
OUTPUT_DIR="docs/HTML"

# Ensure output directory exists
mkdir -p "$OUTPUT_DIR"

# Build classpath from Maven dependencies
mvn dependency:build-classpath -Dmdep.outputFile=classpath.tmp

# Read the classpath
CLASSPATH=$(cat classpath.tmp)

# Generate Javadoc
javadoc -d "$OUTPUT_DIR" \
        -sourcepath "$SRC_DIR" \
        -subpackages com.vserdiuk.casestudy \
        -classpath "$CLASSPATH" \
        -private \
        -charset "UTF-8" \
        -docencoding "UTF-8" \
        -encoding "UTF-8" \
        -author \
        -version \
        -windowtitle "Product-Order Dummy Project Documentation" \
        -doctitle "<h1>Product-Order Dummy Project Documentation</h1>" \
        -header "<h2>Product-Order Dummy Project</h2>" \
        -link https://docs.oracle.com/en/java/javase/17/docs/api/ \
        -verbose

# Check result
if [ $? -eq 0 ]; then
    echo "✅ Javadoc successfully generated in $OUTPUT_DIR"
else
    echo "❌ Error: Javadoc generation failed"
    exit 1
fi
