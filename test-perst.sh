#!/usr/bin/env bash
#
# test-perst.sh - Run Perst/CDatabase tests
#
# Usage:
#   ./test-perst.sh           # Run all Perst tests
#   ./test-perst.sh build     # Build project first
#   ./test-perst.sh cversion  # Run CDatabase versioning test only
#   ./test-perst.sh unit      # Run unit tests via JAR
#   ./test-perst.sh clean     # Clean test database

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Classpath
CP="work/exploded/WEB-INF/classes"
for jar in libs/*.jar; do
    CP="$CP:$jar"
done

echo_perst() {
    echo -e "${GREEN}[Perst]${NC} $1"
}

echo_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error_exit() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
    exit 1
}

# Build the project
do_build() {
    echo_perst "Building project..."
    ./bld build
    echo_perst "Build complete"
}

# Clean Perst database files
do_clean() {
    echo_perst "Cleaning Perst database files..."
    
    # Find and remove Perst database files
    if [ -f "data/oodb" ]; then
        rm -f data/oodb
        echo_perst "Removed: data/oodb"
    fi
    
    if [ -d "data/oodb.idx" ]; then
        rm -rf data/oodb.idx
        echo_perst "Removed: data/oodb.idx/"
    fi
    
    # Also check relative path from backend
    if [ -f "src/main/backend/data/oodb" ]; then
        rm -f src/main/backend/data/oodb
        echo_perst "Removed: src/main/backend/data/oodb"
    fi
    
    if [ -d "src/main/backend/data/oodb.idx" ]; then
        rm -rf src/main/backend/data/oodb.idx
        echo_perst "Removed: src/main/backend/data/oodb.idx/"
    fi
    
    echo_perst "Clean complete"
}

# Run CDatabase versioning test
do_cversion() {
    echo_perst "Running CDatabase versioning test..."
    
    # Check if compiled
    if [ ! -f "work/exploded/WEB-INF/classes/oodb/CDatabaseVersioningTest.class" ]; then
        echo_perst "Compiling test..."
        mkdir -p work/exploded/WEB-INF/classes
        javac -cp "$CP" src/test/core/oodb/CDatabaseVersioningTest.java -d work/exploded/WEB-INF/classes
    fi
    
    # Run the test
    java -cp "$CP" oodb.CDatabaseVersioningTest
}

# Run unit tests via JAR
do_unit() {
    echo_perst "Running Perst unit tests..."
    
    if [ ! -f "work/KissUnitTest.jar" ]; then
        echo_warn "KissUnitTest.jar not found. Run './bld unit-tests' first"
        exit 1
    fi
    
    java -jar work/KissUnitTest.jar --select-package=oodb
}

# Run all Perst tests (unit + integration)
do_all() {
    echo_perst "Running all Perst tests..."
    
    # Build first
    do_build
    
    # Clean DB
    do_clean
    
    # Run unit tests
    do_unit
    
    # Run CDatabase versioning test
    do_cversion
}

# Show status of database files
do_status() {
    echo_perst "Checking Perst database status..."
    
    echo ""
    echo "Database file:"
    if [ -f "data/oodb" ]; then
        echo -e "  ${GREEN}data/oodb${NC} - EXISTS ($(stat -c%s data/oodb 2>/dev/null || stat -f%z data/oodb 2>/dev/null) bytes)"
    else
        echo -e "  data/oodb - NOT FOUND"
    fi
    
    echo ""
    echo "Lucene index:"
    if [ -d "data/oodb.idx" ]; then
        echo -e "  ${GREEN}data/oodb.idx/${NC} - EXISTS"
        echo "  Files:"
        ls -la data/oodb.idx/ 2>/dev/null | head -10 | sed 's/^/    /'
    else
        echo "  data/oodb.idx/ - NOT FOUND"
    fi
    
    # Check from backend perspective too
    echo ""
    echo "From backend perspective (src/main/backend/):"
    if [ -f "src/main/backend/data/oodb" ]; then
        echo -e "  ${GREEN}src/main/backend/data/oodb${NC} - EXISTS"
    else
        echo "  src/main/backend/data/oodb - NOT FOUND"
    fi
    
    if [ -d "src/main/backend/data/oodb.idx" ]; then
        echo -e "  ${GREEN}src/main/backend/data/oodb.idx/${NC} - EXISTS"
    else
        echo "  src/main/backend/data/oodb.idx/ - NOT FOUND"
    fi
}

# Show help
show_help() {
    echo "Usage: ./test-perst.sh [command]"
    echo ""
    echo "Commands:"
    echo "  build     - Build the project"
    echo "  clean     - Clean Perst database files (data/oodb, data/oodb.idx)"
    echo "  cversion  - Run CDatabase versioning integration test"
    echo "  unit      - Run Perst unit tests via KissUnitTest.jar"
    echo "  status    - Show status of Perst database files"
    echo "  all       - Run build + clean + unit tests + cversion test"
    echo "  help      - Show this help"
    echo ""
    echo "Examples:"
    echo "  ./test-perst.sh            # Run all tests"
    echo "  ./test-perst.sh cversion   # Run just CDatabase test"
    echo "  ./test-perst.sh status     # Check DB status"
}

# Main
case "${1:-help}" in
    build)
        do_build
        ;;
    clean)
        do_clean
        ;;
    cversion)
        do_cversion
        ;;
    unit)
        do_unit
        ;;
    status)
        do_status
        ;;
    all)
        do_all
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "Unknown command: $1"
        show_help
        exit 1
        ;;
esac