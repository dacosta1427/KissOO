# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Plan: Continuous Directory Code Cleanup

## Objective
Identify and fix all compile warnings and errors in the `continuous/` directory.

## Current State
- The `continuous/` directory was added to Maven build
- Multiple compile errors due to Lucene API changes (Lucene 9.x vs older version)
- Several unchecked type warnings

---

## Files with Issues

### 1. CDatabase.java
**Errors (9):**
- Line 17: `package org.apache.lucene.queryParser does not exist` - Lucene 9.x removed queryParser package
- Line 18: `package org.apache.lucene.analysis does not exist` - Lucene 9.x reorganized analysis package
- Line 646, 662, 666: `cannot find symbol` - related to Lucene API changes

**Warnings (6):**
- Line 207: Unchecked call to add(V)
- Line 285: Unchecked cast
- Line 384, 420: Unchecked conversion
- Line 468: Unchecked call to execute

**Solution:** Update to Lucene 9.x query parser API or use SimpleQueryParser

---

### 2. FullTextSearchIterator.java
**Errors (4):**
- Line 6, 13, 84: `cannot find symbol` - Lucene 9.x API changes

**Solution:** Update to Lucene 9.x search API

---

### 3. PerstDirectory.java
**Errors (3):**
- Line 45: `cannot find symbol` - Lucene 9.x IndexWriter/Directory API changes

**Warnings (3):**
- Line 230, 254: Unchecked call to set/put
- Line 315: Deprecated item not annotated with @Deprecated

**Solution:** Update to Lucene 9.x Directory API

---

### 4. TableDescriptor.java
**Errors (5):**
- Line 423, 433, 454, 459, 464, 468: `cannot find symbol` - Lucene 9.x API changes

**Warnings (3):**
- Line 230, 476: Unchecked call to getAnnotation
- Line 397: Unchecked call to addIndex

**Solution:** Update to Lucene 9.x API

---

### 5. CVersion.java
**Warnings (2):**
- Line 17: Unchecked conversion
- Line 27: Unchecked cast

**Solution:** Add proper generic type parameters

---

### 6. CVersionHistory.java
**Warnings (3):**
- Line 27, 61: Unchecked cast

**Solution:** Add proper generic type parameters

---

### 7. ExtentIterator.java
**Warnings (1):**
- Line 136: Unchecked cast

**Solution:** Add proper generic type parameters

---

### 8. IndexIterator.java
**Warnings (1):**
- Line 129: Unchecked cast

**Solution:** Add proper generic type parameters

---

## Summary

| File | Errors | Warnings | Priority |
|------|--------|----------|----------|
| CDatabase.java | 9 | 6 | High |
| FullTextSearchIterator.java | 4 | 0 | High |
| PerstDirectory.java | 3 | 3 | High |
| TableDescriptor.java | 5 | 3 | High |
| CVersion.java | 0 | 2 | Low |
| CVersionHistory.java | 0 | 2 | Low |
| ExtentIterator.java | 0 | 1 | Low |
| IndexIterator.java | 0 | 1 | Low |

## Root Cause
The continuous module uses old Lucene 4.x APIs that have been removed/changed in Lucene 9.x.

## Options

1. **Update to Lucene 9.x** - Requires updating query parser, analyzer, and directory APIs
2. **Keep old Lucene version** - Add old Lucene as separate dependency (not recommended)
3. **Remove continuous module** - If not needed, can be excluded from build

## Success Criteria
- [ ] All compile errors fixed
- [ ] All compile warnings reduced or addressed
- [ ] All tests pass
