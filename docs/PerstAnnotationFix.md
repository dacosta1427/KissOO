# Perst JAR Annotation Fix - Documentation

## Problem

The current Perst JAR (`perst-dcg-4.0.0.jar`) is missing two critical annotations needed for CDatabase:

1. **`@Indexable`** - should be in `org.garret.perst.continuous` package (not `org.garret.perst`)
2. **`@FullTextSearchable`** - should be in `org.garret.perst.continuous` package as an annotation

Currently:
- `@Indexable` exists in `org.garret.perst` (for standard Perst)
- `@FullTextSearchable` exists in `org.garret.perst.fulltext` but as an **interface**, not annotation
- The continuous package classes exist but lack the annotation definitions

## Source Files to Add

The annotations exist in the Perst source at:
- `/home/dacosta/Projects/perst439-jdk19/src/org/garret/perst/Indexable.java` (already in JAR as annotation)
- `/home/dacosta/Projects/perst439-jdk19/continuous/src/org/garret/perst/continuous/FullTextSearchable.java`

### 1. FullTextSearchable Annotation

**Source location**: `/home/dacosta/Projects/perst439-jdk19/continuous/src/org/garret/perst/continuous/FullTextSearchable.java`

```java
package org.garret.perst.continuous;

import java.lang.annotation.*;

/**
  * Annotation for full text searchable field
  */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FullTextSearchable { 
}
```

This needs to be compiled and added to the JAR at: `org/garret/perst/continuous/FullTextSearchable.class`

## How to Fix the JAR

### Option A: Add to existing JAR

```bash
# 1. Compile the annotation
cd /home/dacosta/Projects/perst439-jdk19/continuous/src
javac -d /tmp/perst-fix org/garret/perst/continuous/FullTextSearchable.java

# 2. Add to the JAR
cd /home/dacosta/Projects/KissOO/libs
jar uvf perst-dcg-4.0.0.jar -C /tmp/perst-fix org
```

### Option B: Replace the class in JAR

The JAR already contains `org/garret/perst/continuous/FullTextSearchable.class` but it's an interface, not an annotation. 

```bash
# Check what's currently in the JAR
jar tf perst-dcg-4.0.0.jar | grep FullTextSearchable

# Replace with the annotation version
jar uvf perst-dcg-4.0.0.jar /tmp/perst-fix/org/garret/perst/continuous/FullTextSearchable.class
```

## Verification

After fixing, verify with:

```bash
# Should show "interface ... extends Annotation"
javap -cp perst-dcg-4.0.0.jar org.garret.perst.continuous.FullTextSearchable

# Should show both annotations
javap -cp perst-dcg-4.0.0.jar org.garret.perst.continuous.Indexable
javap -cp perst-dcg-4.0.0.jar org.garret.perst.continuous.FullTextSearchable
```

## Required Imports in Domain Classes

Once fixed, use these imports in domain classes:

```java
import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
```

## References

- Perst 4.3.9 source with annotations: `/home/dacosta/Projects/perst439-jdk19/`
- Current JAR: `/home/dacosta/Projects/KissOO/libs/perst-dcg-4.0.0.jar`
- Working example: `/home/dacosta/Projects/perstLatest/continuous/tst/SimpleRelation.java`
