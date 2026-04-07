# PROJECT_DESCRIPTION.md

## Project Overview

**Name:** KissOO  
**Type:** Full-stack web application using Perst OODBMS  
**Language:** Java 17+, Groovy, Svelte 5, TypeScript  
**Build:** Custom build system (./bld)  

**Repository:** https://github.com/dacosta/KissOO

## Purpose

KissOO is a fork of the Kiss web framework with Perst OODBMS integration. It replaces traditional SQL databases with object-oriented persistence, enabling pure OO navigation patterns throughout the codebase.

## Core Features

- **Perst OODBMS** - Object-oriented database with versioning and Lucene integration
- **Manager at Gate** - Three-layer authorization pattern (EndpointMethod → Agreement → Manager)
- **Session-Based Auth** - UUID-only API, backend derives all context from session
- **Svelte 5 Frontend** - Modern reactive UI with runes ($state, $derived, $props)
- **Multi-Language Support** - Backend services in Java, Groovy, Lisp (Lisp disabled)
- **Email Verification** - Token-based user activation pattern

## Architecture Highlights

```
Frontend (Svelte 5)
        ↓ JSON + _uuid
Backend Services (Groovy/Java)
        ↓
Managers (PerstUserManager, ActorManager, ...)
        ↓
PerstStorageManager (singleton)
        ↓
Perst CDatabase (OODBMS)
```

## Getting Started

```bash
# Build
./bld build

# Run in development
./bld develop

# Clear database (when schema changes)
pkill -9 java
rm -rf /home/dacosta/kissoo-data/oodb*
mkdir -p /home/dacosta/kissoo-data
./bld develop
```

## Documentation

| Doc | Purpose |
|-----|---------|
| `AP.md` | Quick reference, entry point |
| `KISSOO_DEVELOPMENT_PROTOCOL.md` | Master rules, patterns, anti-patterns |
| `docs/PerstIntegration.md` | Perst setup and configuration |
| `docs/PerstDeveloperGuide.md` | Developer guide for Perst usage |
| `MANAGER_AT_THE_GATE.md` | Authorization pattern details |

---

*Update only when core identity changes (new tech stack, major architectural shifts).*