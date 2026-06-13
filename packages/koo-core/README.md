# Koo Framework Core

Pure OO framework with ProtoBuf support for KissOO.

## Current Status

**Phase 1: ProtoBuf Foundation** - In Progress

### Completed
- Project structure created
- ProtoBuf schema classes implemented
- Basic schema generator

### Next Steps
- Implement full schema generator with nested object support
- Add Lombok integration
- Create service infrastructure

## Architecture

Uses KISS framework's native binary support:
- `ProcessServlet.returnBinary()` for server
- `Server.binaryCall()` for client

No Base64 encoding needed - raw binary transmission.