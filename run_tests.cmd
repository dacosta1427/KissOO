@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM Perst Test Runner Script (Windows CMD version)
REM Mirrors the behavior of run_tests.sh for Windows environments

REM Resolve project root (directory of this script)
set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%.") do set "PERST_HOME=%%~fI"

REM Move to test directory for execution context
pushd "%PERST_HOME%\tst"

REM Build classpath - compiled classes + libs + current dir
set "CP=%PERST_HOME%\target\classes;%PERST_HOME%\target\test-classes;%PERST_HOME%\lib\perst.jar;%PERST_HOME%\lib\javassist.jar;."

REM Track results
set /a passed=0
set /a failed=0

REM Cleanup function: remove database and temp files used by tests
:cleanup
REM Suppress errors if files don't exist
2>nul (
  del /Q *.dbs
  del /Q *.dbz*
  del /Q *.res
  del /Q *.app
  del /Q testblob.dbs
  del /Q testblob.dbz*
  del /Q testalloc.mfd
  del /Q testautoindices.dbs
  del /Q testcodegenerator.dbs
  del /Q testconcur.dbs
)
goto :eof

REM Run a single test with optional parameters and capture output in %TEMP%
:run_test
set "TEST=%~1"
shift
set "PARAMS=%*"

echo.
echo Running %TEST% %PARAMS%...

if defined PARAMS (
  set "LOG=%TEMP%\test_%TEST%_%PARAMS: =_%.log"
  call :_exec "%TEST%" "%PARAMS%" "%LOG%"
) else (
  set "LOG=%TEMP%\test_%TEST%.log"
  call :_exec "%TEST%" "" "%LOG%"
)

if errorlevel 1 (
  echo [FAIL] %TEST% %PARAMS%
  set /a failed+=1
  echo --- Error output (last 20 lines) ---
  powershell -NoProfile -Command "Get-Content -Path '%LOG%' -Tail 20 | ForEach-Object { $_ }"
) else (
  echo [PASS] %TEST% %PARAMS%
  set /a passed+=1
)
goto :eof

REM Internal: execute Java with proper params and logging; set errorlevel on failure
:_exec
setlocal
set "_T=%~1"
set "_P=%~2"
set "_L=%~3"
if defined _P (
  REM Use cmd /c to ensure proper parameter expansion and redirection
  cmd /c "java -cp "%CP%" %_T% %_P% 1>"%_L%" 2>&1"
) else (
  cmd /c "java -cp "%CP%" %_T% 1>"%_L%" 2>&1"
)
endlocal & exit /b %errorlevel%


echo Running Perst Tests...
echo ======================

REM ============================================================================
REM Tests from original run_tests.sh (39 tests)
REM ============================================================================

call :cleanup
call :run_test Simple

call :cleanup
call :run_test Benchmark

call :cleanup
call :run_test TestIndex

call :cleanup
call :run_test TestIndex2

call :cleanup
call :run_test TestAgg

call :cleanup
call :run_test TestBackup

call :run_test TestBit

call :run_test TestBitmap

call :cleanup
call :run_test TestBlob

call :run_test TestCompoundIndex

call :cleanup
call :run_test TestFullTextIndex

call :cleanup
call :run_test TestGC

call :cleanup
call :run_test TestJSQL

call :run_test TestKDTree

REM SKIPPED - TestLink - needs investigation
REM call :run_test TestLink

call :cleanup
call :run_test TestList

call :cleanup
call :run_test TestMap

call :run_test TestMaxOid

call :run_test TestMod

call :run_test TestPatricia

call :run_test TestR2

call :cleanup
call :run_test TestRaw

call :run_test TestRecovery

call :run_test TestRegex

call :run_test TestReplic

call :run_test TestRndIndex

call :cleanup
call :run_test TestRollback

call :run_test TestRtree

call :run_test TestSet

call :run_test TestSSD

call :run_test TestThickIndex

call :cleanup
call :run_test TestTimeSeries

call :run_test TestTtree

call :run_test TestVersion

call :cleanup
call :run_test TestXML

call :run_test SearchEngine

call :run_test IpCountry

call :run_test Guess

call :run_test AstroNet

REM ============================================================================
REM Tests MISSING from original run_tests.sh (19 tests)
REM ============================================================================

call :cleanup
call :run_test TestAlloc

call :cleanup
call :run_test TestAutoIndices

call :cleanup
call :run_test TestCodeGenerator

call :cleanup
call :run_test TestConcur

call :cleanup
call :run_test TestDbServer

call :cleanup
call :run_test TestDecimal

call :cleanup
call :run_test TestDerivedIndex

call :cleanup
call :run_test TestDynamicObjects

call :cleanup
call :run_test TestIndexIterator

call :cleanup
call :run_test TestJSQLContains

call :cleanup
call :run_test TestJsqlJoin

REM Run TestJsqlJoin twice (as per makefile)
call :cleanup
call :run_test TestJsqlJoin

call :cleanup
call :run_test TestKDTree2

call :cleanup
call :run_test TestLeak

call :cleanup
call :run_test TestLoad

call :cleanup
call :run_test TestPerf

call :cleanup
call :run_test TestRandomBlob

call :cleanup
call :run_test TestReplic2

call :cleanup
call :run_test TestServer

call :cleanup
call :run_test TestSOD

REM ============================================================================
REM Test variants with parameters (from makefile analysis)
REM ============================================================================

REM TestIndex variants
call :cleanup
call :run_test TestIndex altbtree

call :cleanup
call :run_test TestIndex inmemory

call :cleanup
call :run_test TestIndex map

call :cleanup
call :run_test TestIndex zip

call :cleanup
call :run_test TestIndex multifile

call :cleanup
call :run_test TestIndex gc

REM TestMap variants
call :cleanup
call :run_test TestMap populate

call :cleanup
call :run_test TestMap 100

call :cleanup
call :run_test TestMap 100 populate

REM TestKDTree variants
call :cleanup
call :run_test TestKDTree populate

REM TestKDTree2 variants
call :cleanup
call :run_test TestKDTree2 populate

REM TestGC variants
call :cleanup
call :run_test TestGC background

call :cleanup
call :run_test TestGC altbtree background

REM TestCompoundIndex variants
call :run_test TestCompoundIndex altbtree

REM TestIndexIterator variants
call :cleanup
call :run_test TestIndexIterator altbtree

REM TestDynamicObjects variants
call :cleanup
call :run_test TestDynamicObjects populate

call :cleanup
call :run_test TestDynamicObjects

REM TestFullTextIndex variants
call :cleanup
call :run_test TestFullTextIndex reload

REM TestMod variants
call :run_test TestMod pinned

REM TestPerf variants
call :cleanup
call :run_test TestPerf inmemory

REM ============================================================================
REM Summary
REM ============================================================================

echo.
echo ======================
echo Test Results: %passed% passed, %failed% failed

popd

if %failed% GTR 0 (
  echo Some tests failed. Check %%TEMP%%\test_*.log for details.
  exit /b 1
)

echo All tests passed!
exit /b 0
