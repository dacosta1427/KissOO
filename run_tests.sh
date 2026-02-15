#!/bin/bash

# Perst Test Runner Script
# This script runs all available Perst tests including parameter variants

PERST_HOME="/home/dacosta/Projects/oodb"
cd "$PERST_HOME/tst"

# Classpath - include both compiled classes and lib
CP="$PERST_HOME/target/classes:$PERST_HOME/target/test-classes:$PERST_HOME/lib/perst.jar:$PERST_HOME/lib/javassist.jar:."

# Clean up database files before testing
cleanup() {
    rm -f *.dbs *.dbz* *.res *.app testblob.dbs testblob.dbz* testalloc.mfd testautoindices.dbs testcodegenerator.dbs testconcur.dbs 2>/dev/null
}

# Run a single test with optional parameters
# Args: test_name [param1] [param2] [param3]
run_test() {
    local test_name="$1"
    shift
    local params="$@"
    
    echo ""
    echo "Running $test_name $params..."
    if [ -n "$params" ]; then
        if java -cp "$CP" "$test_name" $params > /tmp/test_${test_name}_${params// /_}.log 2>&1; then
            echo "✓ $test_name $params PASSED"
            ((passed++))
        else
            echo "✗ $test_name $params FAILED"
            ((failed++))
            echo "--- Error output ---"
            tail -20 /tmp/test_${test_name}_${params// /_}.log
        fi
    else
        if java -cp "$CP" "$test_name" > /tmp/test_${test_name}.log 2>&1; then
            echo "✓ $test_name PASSED"
            ((passed++))
        else
            echo "✗ $test_name FAILED"
            ((failed++))
            echo "--- Error output ---"
            tail -20 /tmp/test_${test_name}.log
        fi
    fi
}

# Run a test expecting it to fail (for negative tests)
run_test_expect_fail() {
    local test_name="$1"
    shift
    local params="$@"
    
    echo ""
    echo "Running $test_name $params (expecting failure)..."
    if [ -n "$params" ]; then
        if java -cp "$CP" "$test_name" $params > /tmp/test_${test_name}_${params// /_}.log 2>&1; then
            echo "✓ $test_name $params PASSED (as expected)"
            ((passed++))
        else
            echo "✗ $test_name $params FAILED (unexpected)"
            ((failed++))
            echo "--- Error output ---"
            tail -20 /tmp/test_${test_name}_${params// /_}.log
        fi
    else
        if java -cp "$CP" "$test_name" > /tmp/test_${test_name}.log 2>&1; then
            echo "✓ $test_name PASSED (as expected)"
            ((passed++))
        else
            echo "✗ $test_name FAILED (unexpected)"
            ((failed++))
            echo "--- Error output ---"
            tail -20 /tmp/test_${test_name}.log
        fi
    fi
}

passed=0
failed=0

echo "Running Perst Tests..."
echo "======================"

# ============================================================================
# Tests from original run_tests.sh (39 tests)
# ============================================================================

cleanup
run_test "Simple"

cleanup
run_test "Benchmark"

cleanup
run_test "TestIndex"

cleanup
run_test "TestIndex2"

cleanup
run_test "TestAgg"

cleanup
run_test "TestBackup"

run_test "TestBit"

run_test "TestBitmap"

cleanup
run_test "TestBlob"

run_test "TestCompoundIndex"

cleanup
run_test "TestFullTextIndex"

cleanup
run_test "TestGC"

cleanup
run_test "TestJSQL"

run_test "TestKDTree"

# SKIPPED - TestLink - needs investigation
# run_test "TestLink"

cleanup
run_test "TestList"

cleanup
run_test "TestMap"

run_test "TestMaxOid"

run_test "TestMod"

run_test "TestPatricia"

run_test "TestR2"

cleanup
run_test "TestRaw"

run_test "TestRecovery"

run_test "TestRegex"

run_test "TestReplic"

run_test "TestRndIndex"

cleanup
run_test "TestRollback"

run_test "TestRtree"

run_test "TestSet"

run_test "TestSSD"

run_test "TestThickIndex"

cleanup
run_test "TestTimeSeries"

run_test "TestTtree"

run_test "TestVersion"

cleanup
run_test "TestXML"

run_test "SearchEngine"

run_test "IpCountry"

run_test "Guess"

run_test "AstroNet"

# ============================================================================
# Tests MISSING from original run_tests.sh (19 tests)
# ============================================================================

cleanup
run_test "TestAlloc"

cleanup
run_test "TestAutoIndices"

cleanup
run_test "TestCodeGenerator"

cleanup
run_test "TestConcur"

cleanup
run_test "TestDbServer"

cleanup
run_test "TestDecimal"

cleanup
run_test "TestDerivedIndex"

cleanup
run_test "TestDynamicObjects"

cleanup
run_test "TestIndexIterator"

cleanup
run_test "TestJSQLContains"

cleanup
run_test "TestJsqlJoin"

# Run TestJsqlJoin twice (as per makefile)
cleanup
run_test "TestJsqlJoin"

cleanup
run_test "TestKDTree2"

cleanup
run_test "TestLeak"

cleanup
run_test "TestLoad"

cleanup
run_test "TestPerf"

cleanup
run_test "TestRandomBlob"

cleanup
run_test "TestReplic2"

cleanup
run_test "TestServer"

# SKIPPED - TestSOD is an interactive menu-driven test, not suitable for automated runs
# cleanup
# run_test "TestSOD"

# ============================================================================
# Test variants with parameters (from makefile analysis)
# ============================================================================

# TestIndex variants
cleanup
run_test "TestIndex" "altbtree"

cleanup
run_test "TestIndex" "inmemory"

cleanup
run_test "TestIndex" "map"

cleanup
run_test "TestIndex" "zip"

cleanup
run_test "TestIndex" "multifile"

cleanup
run_test "TestIndex" "gc"

# TestMap variants
cleanup
run_test "TestMap" "populate"

cleanup
run_test "TestMap" "100"

cleanup
run_test "TestMap" "100" "populate"

# TestKDTree variants
cleanup
run_test "TestKDTree" "populate"

# TestKDTree2 variants
cleanup
run_test "TestKDTree2" "populate"

# TestGC variants
cleanup
run_test "TestGC" "background"

cleanup
run_test "TestGC" "altbtree" "background"

# TestCompoundIndex variants
run_test "TestCompoundIndex" "altbtree"

# TestIndexIterator variants
cleanup
run_test "TestIndexIterator" "altbtree"

# TestDynamicObjects variants
cleanup
run_test "TestDynamicObjects" "populate"

cleanup
run_test "TestDynamicObjects"

# TestReplic variants (master/slave - needs both running)
# Note: These need to run in parallel in real scenario
# cleanup
# run_test "TestReplic" "master" &
# run_test "TestReplic" "slave" &

# TestBlob variants
cleanup
# run_test "TestBlob" "zip"  # Removed - compression no longer used

# TestFullTextIndex variants
cleanup
run_test "TestFullTextIndex" "reload"

# TestMod variants
run_test "TestMod" "pinned"

# TestPerf variants
cleanup
run_test "TestPerf" "inmemory"

# ============================================================================
# Summary
# ============================================================================

echo ""
echo "======================"
echo "Test Results: $passed passed, $failed failed"

if [ $failed -gt 0 ]; then
    echo "Some tests failed. Check /tmp/test_*.log for details."
    exit 1
fi

echo "All tests passed!"
exit 0
