#!/bin/bash

# Perst Test Runner Script - Quick version with timeouts
# This script runs all available Perst tests including parameter variants

PERST_HOME="/home/dacosta/Projects/oodb"
cd "$PERST_HOME/tst"

# Classpath - include both compiled classes and lib
CP="$PERST_HOME/target/classes:$PERST_HOME/target/test-classes:$PERST_HOME/lib/perst.jar:$PERST_HOME/lib/javassist.jar:."

# Clean up database files before testing
cleanup() {
    rm -f *.dbs *.dbz* *.res *.app testblob.dbs testblob.dbz* testalloc.mfd testautoindices.dbs testcodegenerator.dbs testconcur.dbs 2>/dev/null
}

# Run a single test with optional parameters and timeout
# Args: test_name [param1] [param2] [param3]
run_test() {
    local test_name="$1"
    shift
    local params="$@"
    
    cleanup
    
    echo -n "Testing $test_name "
    [ -n "$params" ] && echo -n "$params "
    echo -n "... "
    
    if [ -n "$params" ]; then
        timeout 30 java -cp "$CP" "$test_name" $params > /tmp/test_${test_name}_${params// /_}.log 2>&1
    else
        timeout 30 java -cp "$CP" "$test_name" > /tmp/test_${test_name}.log 2>&1
    fi
    
    if [ $? -eq 0 ]; then
        echo "PASSED"
        ((passed++))
    else
        echo "FAILED"
        ((failed++))
    fi
}

passed=0
failed=0

echo "Running Perst Tests (Quick Mode with 30s timeout)..."
echo "======================================================"

# ============================================================================
# Tests from original run_tests.sh (39 tests)
# ============================================================================

run_test "Simple"
run_test "Benchmark"
run_test "TestIndex"
run_test "TestIndex2"
run_test "TestAgg"
run_test "TestBackup"
run_test "TestBit"
run_test "TestBitmap"
run_test "TestBlob"
run_test "TestCompoundIndex"
run_test "TestFullTextIndex"
run_test "TestGC"
run_test "TestJSQL"
run_test "TestKDTree"

# SKIPPED - TestLink - as requested
echo "Testing TestLink ... SKIPPED"

run_test "TestList"
run_test "TestMap"
run_test "TestMaxOid"
run_test "TestMod"
run_test "TestPatricia"
run_test "TestR2"
run_test "TestRaw"
run_test "TestRecovery"
run_test "TestRegex"
run_test "TestReplic"
run_test "TestRndIndex"
run_test "TestRollback"
run_test "TestRtree"
run_test "TestSet"
run_test "TestSSD"
run_test "TestThickIndex"
run_test "TestTimeSeries"
run_test "TestTtree"
run_test "TestVersion"
run_test "TestXML"
run_test "SearchEngine"
run_test "IpCountry"
run_test "Guess"
run_test "AstroNet"

# ============================================================================
# Tests MISSING from original run_tests.sh (19 tests)
# ============================================================================

run_test "TestAlloc"
run_test "TestAutoIndices"
run_test "TestCodeGenerator"
run_test "TestConcur"
run_test "TestDbServer"
run_test "TestDecimal"
run_test "TestDerivedIndex"
run_test "TestDynamicObjects"
run_test "TestIndexIterator"
run_test "TestJSQLContains"
run_test "TestJsqlJoin"
run_test "TestKDTree2"
run_test "TestLeak"
run_test "TestLoad"
run_test "TestPerf"
run_test "TestRandomBlob"
run_test "TestReplic2"
run_test "TestServer"
run_test "TestSOD"

# ============================================================================
# Test variants with parameters (from makefile analysis)
# ============================================================================

run_test "TestIndex" "altbtree"
run_test "TestIndex" "inmemory"
run_test "TestIndex" "map"
run_test "TestIndex" "zip"
run_test "TestIndex" "multifile"
run_test "TestIndex" "gc"
run_test "TestMap" "populate"
run_test "TestMap" "100"
run_test "TestMap" "100" "populate"
run_test "TestKDTree" "populate"
run_test "TestKDTree2" "populate"
run_test "TestGC" "background"
run_test "TestGC" "altbtree" "background"
run_test "TestCompoundIndex" "altbtree"
run_test "TestIndexIterator" "altbtree"
run_test "TestDynamicObjects" "populate"
# run_test "DynamicObjects"  # REMOVED - typo, should be TestDynamicObjects (already run above)
# run_test "TestBlob" "zip"  # Removed - compression no longer used
run_test "TestFullTextIndex" "reload"
run_test "TestMod" "pinned"
run_test "TestPerf" "inmemory"

# ============================================================================
# Summary
# ============================================================================

echo ""
echo "======================================================"
echo "Test Results: $passed passed, $failed failed"
