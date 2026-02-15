# Perst Project Analysis and Recommendations

> See [workingRules.md](./workingRules.md) for core operating principles

---

## Project Overview

**Project:** Perst (Object-Oriented Database)  
**Type:** Java Object Database Management System (OODBMS)  
**Version:** 4.39  
**Current Java Target:** 1.6/1.4 (severely outdated)  
**Current Environment:** OpenJDK 25

---

## Current State Analysis

### Directory Structure
```
oodb/
├── src/              # Main Perst library (Java 1.6)
├── tst/              # Test suite
├── lib/              # JAR dependencies (manual)
├── doc/              # Documentation
├── lucene/           # Lucene integration
├── javassist/        # Bytecode manipulation
├── jzlib/            # Compression library
├── assoc/            # Association database
├── continuous/       # Continuous versioning
├── junit_tests/      # JUnit tests
├── pom.xml           # Maven config (exists but not primary)
├── makefile          # Legacy build (primary)
└── build.xml         # Ant build
```

### Key Issues Identified

1. **Legacy Build System**
   - Primary: makefile + build.xml (Ant)
   - Secondary: pom.xml (Maven, unused)

2. **Outdated Dependencies**
   - Lucene 4.7.2 (released 2014)
   - Javassist 3.18.1-GA (released 2013)
   - AspectJ 1.7.4 (released 2013)

3. **Deprecated Java APIs**
   - Uses `Hashtable` (1.2+)
   - Source level 1.6/1.4

4. **Missing Modern Practices**
   - No CI/CD
   - No GitHub Actions
   - No automated tests in CI

---

## Recommendations (Priority Order)

### Priority #1: Modernize Build System
**Impact:** HIGH | **Effort:** MEDIUM

- Migrate from Ant/makefile to Maven/Gradle
- Use pom.xml as single source of truth
- Benefits:
  - Dependency resolution
  - Standard build process
  - Easy Java version upgrades

### Priority #2: Upgrade Java Version
**Impact:** HIGH | **Effort:** LOW

- Update from Java 1.6 → Java 25
- Update pom.xml compiler settings
- Test compilation

### Priority #3: Update Dependencies
**Impact:** MEDIUM | **Effort:** HIGH

- Lucene 4.7.2 → 9.x (breaking changes expected)
- Javassist 3.18.1 → 3.33.x
- AspectJ 1.7.4 → 1.9.x

### Priority #4: Replace Deprecated APIs
**Impact:** MEDIUM | **Effort:** LOW

- Replace `Hashtable` → `HashMap`
- Replace `Vector` → `ArrayList`
- Replace `Enumeration` → `Iterator`

### Priority #5: Add Modern Testing
**Impact:** MEDIUM | **Effort:** MEDIUM

- Add JUnit 5
- Add GitHub Actions CI workflow
- Add code coverage reporting

---

## Immediate Next Steps

1. Create `md/` directory for documentation ✓
2. Modernize build system (Priority #1)
3. Upgrade Java to 25 (Priority #2)
4. Update dependencies (Priority #3)
5. Run tests to verify functionality

---

## Notes

- Perst is a commercial-grade OODBMS by McObject LLC
- Well-established codebase with extensive features
- Migration to modern Java will ensure longevity
- Consider incremental approach: build system first, then dependencies
