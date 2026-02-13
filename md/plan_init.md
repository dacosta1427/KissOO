> See [workingRules.md](./workingRules.md) for core operating principles

---

# Plan: Perst OpenJDK 25 Migration

## Objective
Modernize Perst project to work with OpenJDK 25, improve code quality, and establish modern development practices.

## Current State
- Java target: 1.6/1.4 (severely outdated)
- Build system: Ant/makefile (legacy)
- Dependencies: Lucene 4.7.2, Javassist 3.18.1, AspectJ 1.7.4 (all 2013-2014)
  - **Note:** A major Lucene upgrade to the latest version is required (Lucene 4.7.2 API has significant breaking changes in newer versions)

## Goals
1. Migrate to OpenJDK 25
2. Modernize build system
3. Update dependencies
4. Replace deprecated APIs
5. Add modern testing/CI

---

## Risks & Rollback Plan

| Task | Risk | Rollback |
|------|------|----------|
| Dependency updates | API breaking changes | Revert pom.xml, restore old versions |
| Java version upgrade | Compilation failures | Revert compiler settings to 1.6 |
| Build system change | Build failures | Keep makefile as backup |

---

## Dependencies
- Priority #1 (Build) → Priority #2 (Java) → Priority #3 (Dependencies) → Priority #4 (Deprecated APIs) → Priority #5 (Testing)
