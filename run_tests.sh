#!/bin/bash

# Perst Test Runner Script
# This script runs all available Perst tests

PERST_HOME="/home/dacosta/Projects/oodb"
cd "$PERST_HOME/tst"

# Classpath
CP="$PERST_HOME/target/classes:$PERST_HOME/target/test-classes:$PERST_HOME/lib/javassist.jar:."

# Run tests
echo "Running Perst Tests..."
echo "======================"

tests=(
    "Simple"
    "Benchmark"
    "TestIndex"
    "TestIndex2"
    "TestAgg"
    "TestBackup"
    "TestBit"
    "TestBitmap"
    "TestBlob"
    "TestCompoundIndex"
    "TestFullTextIndex"
    "TestGC"
    "TestJSQL"
    "TestKDTree"
    "TestLink"
    "TestList"
    "TestMap"
    "TestMaxOid"
    "TestMod"
    "TestPatricia"
    "TestR2"
    "TestRaw"
    "TestRecovery"
    "TestRegex"
    "TestReplic"
    "TestRndIndex"
    "TestRollback"
    "TestRtree"
    "TestSet"
    "TestSSD"
    "TestThickIndex"
    "TestTimeSeries"
    "TestTtree"
    "TestVersion"
    "TestXML"
    "SearchEngine"
    "IpCountry"
    "Guess"
    "AstroNet"
)

passed=0
failed=0

for test in "${tests[@]}"; do
    echo ""
    echo "Running $test..."
    if java -cp "$CP" "$test" > /tmp/test_$test.log 2>&1; then
        echo "✓ $test PASSED"
        ((passed++))
    else
        echo "✗ $test FAILED"
        ((failed++))
        echo "--- Error output ---"
        tail -20 /tmp/test_$test.log
    fi
done

echo ""
echo "======================"
echo "Test Results: $passed passed, $failed failed"