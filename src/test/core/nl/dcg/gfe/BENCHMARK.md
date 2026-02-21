# Perst vs PostgreSQL Benchmark

## Overview

This benchmark compares the performance of **Perst** (an embedded Object-Oriented Database) against **PostgreSQL** (a traditional relational database) for basic CRUD operations.

There are two benchmark suites:
1. **Simple Benchmark** (`ComparisonBenchmark.java`) - Basic integer key operations
2. **Complex Benchmark** (`ComparisonBenchmark2.java`) - Real-world with complex objects and relationships

---

## Benchmark 1: Simple Operations (100K records)

### Test Configuration

- **Records**: 100,000
- **Perst**: Embedded OODB with B-tree index
- **PostgreSQL**: 18.2 (running locally on port 5432)
- **PostgreSQL Index**: B-tree index on `int_key` column
- **Test Database**: `perst_test`

### Results

| Operation | Perst | PostgreSQL | Speedup |
|-----------|-------|------------|---------|
| **Insert** | 530 ms | 13,199 ms | **24.9x** |
| **Search** | 261 ms | 19,658 ms | **75x** |
| **Iteration** | 82 ms | 279 ms | **3.4x** |
| **Delete** | 626 ms | 134 ms | 0.21x (PG wins) |

---

## Benchmark 2: Complex Objects (10K movies, 5K actors)

### Test Configuration

- **Movies**: 10,000
- **Actors**: 5,000
- **Schema**: Movies with year, genre, rating; Actors with name, birth_year, country
- **PostgreSQL Indices**: On year, genre, rating, uuid columns

### Results

| Operation | Perst | PostgreSQL | Speedup |
|-----------|-------|------------|---------|
| **Bulk Insert Movies** | 268 ms | 683 ms | **2.5x** |
| **Bulk Insert Actors** | 115 ms | 286 ms | **2.5x** |
| **Range Query** | 73 ms | 17 ms | 0.23x (PG wins) |
| **Text Search** | 1 ms | 1 ms | 1x |
| **Aggregation** | 11 ms | 11 ms | 1x |
| **Complex Query** | 0 ms | 2 ms | Perst |
| **Update** | 84 ms | 210 ms | **2.5x** |
| **Delete** | 38 ms | 7 ms | 0.18x (PG wins) |

### Key Findings - Complex Objects

- **Bulk Insert**: Perst ~2.5x faster
- **Range Query**: PostgreSQL wins due to proper index utilization
- **Update**: Perst ~2.5x faster
- **Delete**: PostgreSQL bulk delete is ~5x faster

---

## Benchmark 3: Field Index Comparison

### Test Configuration

- **Movies**: 10,000 with proper FieldIndex on year, genre, rating
- **PostgreSQL**: Same indices on corresponding columns
- **Both databases use field-level indices**

### Results

| Operation | Perst | PostgreSQL | Speedup |
|-----------|-------|------------|---------|
| **Bulk Insert** | 388 ms | 935 ms | **2.4x** |
| **Range Query (year)** | 1 ms | 26 ms | **26x** |
| **Range Query (rating)** | 10 ms | 21 ms | **2.1x** |
| **Exact Match (genre)** | 2 ms | 5 ms | **2.5x** |
| Aggregation | 11 ms | 8 ms | 0.73x |
| Name Search | 13 ms | 3 ms | 0.23x |

### Key Finding

**With proper FieldIndex, Perst outperforms PostgreSQL on all indexed queries.**

- Perst's B-tree FieldIndex is highly optimized
- Range queries: Perst 26x faster
- Insert: Perst 2.4x faster

---

## Benchmark 4: Optimistic vs Pessimistic Locking

### Configuration
- Records: 5,000
- Conflict handling: 50% overwrite, 50% leave
- Test simulates multiple threads updating same records

### Results

| Operation | Perst | PostgreSQL | Speedup |
|-----------|-------|------------|---------|
| Baseline Insert | 181 ms | 218 ms | **1.2x** |
| Concurrent Updates (same keys) | 3933 ms | 2515 ms | 0.64x (PG wins) |
| Single Record Update | 209 ms | 13466 ms | **64x** |
| Bulk Update | 36 ms | 40 ms | **1.1x** |

### Key Findings

- **Low contention**: Perst 1-64x faster (no locking overhead)
- **High contention (same keys)**: PostgreSQL 1.6x faster (pessimistic locking)
- **Perst**: Optimistic locking - fails fast on conflict, no blocking
- **PostgreSQL**: Pessimistic locking - blocks but efficient under contention

### Important Note
Perst Storage is not thread-safe - requires thread-local instances for true multi-threaded use.

---

## Benchmark 5: Complex Queries (JOIN, ORDER BY, Aggregation)

### Configuration
- Movies: 5,000, Actors: 2,000, Relationships: 15,000

### Results

| Operation | Perst | PostgreSQL | Speedup |
|-----------|-------|------------|---------|
| JOIN Query | 256 ms | 38 ms | 0.15x (PG wins) |
| ORDER BY + LIMIT | 9 ms | 47 ms | **5.2x** |
| GROUP BY | 15 ms | 12 ms | 0.80x (PG wins) |
| Nested Subquery | 17 ms | 5 ms | 0.29x (PG wins) |
| Complex WHERE | 8 ms | 4 ms | 0.50x (PG wins) |

### Key Findings

- **PostgreSQL wins on complex queries** - optimized query planner, indexes, joins
- **Perst wins on ORDER BY+LIMIT** - in-memory sort is fast
- JOINs are significantly slower in Perst (manual iteration)

---

## Test Environment

```
Perst Version: 4.39
PostgreSQL Version: 18.2
Java Version: 25.0.2
Page Pool Size: 64 MB
```

## Results

| Operation | Perst | PostgreSQL | Speedup |
|-----------|-------|------------|---------|
| **Insert** | 530 ms | 13,199 ms | **24.9x** |
| **Search** | 261 ms | 19,658 ms | **75x** |
| **Iteration** | 82 ms | 279 ms | **3.4x** |
| **Delete** | 626 ms | 134 ms | 0.21x (PG wins) |

## Key Findings

### Perst Advantages
1. **Insert Performance**: Perst is ~25x faster for writes due to:
   - No network latency (embedded database)
   - No SQL parsing overhead
   - Direct object storage without serialization

2. **Search Performance**: Perst is ~75x faster because:
   - In-memory index access
   - No network round-trips
   - Native object retrieval

3. **Iteration**: Perst is ~3.4x faster for full table scans

### PostgreSQL Advantages
- **Bulk Delete**: ~5x faster for large batch deletes
- **ACID Compliance**: Better for complex transactions
- **SQL Standard**: Better tooling and ecosystem

## Running the Benchmark

### Prerequisites

1. PostgreSQL running on localhost:5432
2. Database created: `perst_test`
3. User credentials: `postgres` / `gfe`

### Compile Both Benchmarks

```bash
javac -cp "libs/perst.jar;postgresql-42.7.1.jar" \
  -d target/classes \
  src/test/core/nl/dcg/gfe/ComparisonBenchmark.java

javac -cp "libs/perst.jar;postgresql-42.7.1.jar" \
  -d target/classes \
  src/test/core/nl/dcg/gfe/ComparisonBenchmark2.java
```

### Run Simple Benchmark

```bash
java -cp "libs/perst.jar;postgresql-42.7.1.jar;target/classes" \
  nl.dcg.gfe.ComparisonBenchmark
```

### Run Complex Benchmark

```bash
java -cp "libs/perst.jar;postgresql-42.7.1.jar;target/classes" \
  nl.dcg.gfe.ComparisonBenchmark2
```

### Run Field Index Benchmark

```bash
java -cp "libs/perst.jar;postgresql-42.7.1.jar;target/classes" \
  nl.dcg.gfe.ComparisonBenchmark3
```

### Run Transaction Benchmark

```bash
java -cp "libs/perst.jar;postgresql-42.7.1.jar;target/classes" \
  nl.dcg.gfe.ComparisonBenchmark4
```

### Run Complex Query Benchmark

```bash
java -cp "libs/perst.jar;postgresql-42.7.1.jar;target/classes" \
  nl.dcg.gfe.ComparisonBenchmark5
```

### Custom Parameters

```bash
# Custom JDBC URL
java ... nl.dcg.gfe.ComparisonBenchmark jdbc:postgresql://localhost:5432/mytest

# Custom credentials
java ... nl.dcg.gfe.ComparisonBenchmark --user=myuser --password=mypass

# In-memory Perst (no disk persistence)
java ... nl.dcg.gfe.ComparisonBenchmark inmemory
```

## Test Code

The test performs the following operations:

1. **Insert**: Inserts 100,000 records with sequential integer keys
2. **Search**: Performs 100,000 index lookups by key
3. **Iteration**: Iterates through all records using a cursor
4. **Delete**: Deletes all 100,000 records

Each operation is timed independently, and results are printed with speedup ratios.

## Conclusion

Perst provides significantly better performance for typical application workloads (inserts, reads, iterations). PostgreSQL is preferred when:
- Complex SQL queries are needed
- ACID compliance is critical
- Bulk operations dominate the workload

For embedded applications with object-oriented data models, Perst offers substantial performance gains.
