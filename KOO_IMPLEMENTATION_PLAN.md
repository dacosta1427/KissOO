# Koo Framework Implementation Plan
*A comprehensive guide leveraging KISS's native binary support*

## Executive Summary

**Major Discovery**: KISS framework already has native binary transport via `ProcessServlet.returnBinary()` and `Server.binaryCall()`. This eliminates the need to build custom binary protocols.

**Impact**: Framework becomes simpler, faster, and leverages existing battle-tested infrastructure.

---

## 1. Project Structure

```
koo-framework/
├── packages/
│   ├── koo-core/              # Java framework core
│   │   ├── src/main/java/
│   │   │   └── koo/framework/
│   │   │       ├── domain/    # Domain enhancement layer
│   │   │       ├── proto/     # ProtoBuf generation
│   │   │       ├── services/  # Service infrastructure
│   │   │       ├── ui/        # UI generation engine
│   │   │       └── permissions/ # Agreement integration
│   │   └── pom.xml
│   ├── koo-svelte/            # Svelte 5 components
│   │   ├── src/lib/
│   │   │   ├── components/
│   │   │   │   ├── modals/    # Modal system
│   │   │   │   ├── forms/     # Form components
│   │   │   │   ├── tables/    # DataTable with expandable rows
│   │   │   │   └── navigation/ # Navigation
│   │   │   └── lib/
│   │   └── package.json
│   └── koo-cli/               # Development tools
│       └── src/
├── docs/                      # Documentation
├── examples/                    # Example domain implementations
└── README.md
```

---

## 2. Core Components

### 2.1 ProtoBuf Generation (koo-core/proto)

**Classes to implement:**
- `ProtoSchemaGenerator` - Generates proto schemas from Java domain classes
- `ProtoTemplateEngine` - Creates proto templates
- `ProtoRegistry` - Manages proto schema registry

**Key methods:**
```java
generateSchema(Class<?> domainClass): ProtoSchema
registerSchema(String className, ProtoSchema schema): void
getSchema(Class<?> domainClass): ProtoSchema
```

### 2.2 Domain Enhancement (koo-core/domain)

**Classes to implement:**
- `DomainEnhancer` - Enhances domain classes with persistence
- `FieldAnalyzer` - Analyzes domain class fields
- `ReferenceResolver` - Handles object references

### 2.3 Service Infrastructure (koo-core/services)

**Classes to implement:**
- `ServiceAnalyzer` - Parses service method names to determine UI intent
- `PermissionMapper` - Maps service methods to Agreement permissions
- `ResponseHandler` - Handles proto responses (uses KISS binaryCall)

**Naming conventions:**
- `searchXyz` → Search screen
- `getXyz` → View screen
- `updateXyz`/`createXyz` → Edit modal

### 2.4 UI Generation Engine (koo-core/ui)

**Classes to implement:**
- `ComponentGenerator` - Generates Svelte component templates
- `FieldMapper` - Maps proto fields to UI components
- `LayoutBuilder` - Builds page layouts

### 2.5 Svelte Components (koo-svelte)

**Modal System:**
- `Modal.svelte` - Basic modal container
- `ModalManager.svelte` - Dynamic modal handling
- `ModalStore.js` - State management

**Form Components:**
- `FormField.svelte` - Base form field
- `TextInput.svelte`, `NumberInput.svelte`, etc.
- `FormGenerator.svelte` - Auto-generates forms from proto schemas

**Table Components:**
- `DataTable.svelte` - Main table component
- `ExpandableRow.svelte` - Nested object display
- `TableStore.js` - Table state management

---

## 3. Implementation Steps

### Phase 1: ProtoBuf Foundation (Week 1)
1. **Setup project structure**
   - Create monorepo with yarn/npm workspaces
   - Configure Java build for koo-core
   - Configure Svelte build for koo-svelte

2. **Implement ProtoBuf generation**
   - `ProtoSchemaGenerator` - reflect on domain classes
   - `ProtoTemplateEngine` - generate .proto files
   - `ProtoRegistry` - store and retrieve schemas

### Phase 2: Modal System (Week 2)
1. **Basic modal components**
   - `Modal.svelte` with slot support
   - `ModalManager.svelte` for dynamic content
   - `ModalStore.js` for state management

2. **Integration**
   - Connect to service responses
   - Support KISS binary responses
   - Support nested modals

### Phase 3: Form Components (Week 2-3)
1. **Field components**
   - Text, Number, Date, Select inputs
   - Validation integration
   - Proto type mapping

2. **Form generator**
   - Parse proto schemas
   - Generate form layouts
   - Handle nested objects

### Phase 4: DataTable & Expandable Rows (Week 3)
1. **DataTable component**
   - Column generation from proto
   - Sorting and pagination
   - Row selection

2. **Expandable rows**
   - Lazy loading of nested objects
   - Sub-row rendering
   - Loading states

### Phase 5: Service Integration (Week 4)
1. **Service analyzer**
   - Parse method names
   - Determine UI intent
   - Map to components

2. **Permission integration**
   - Connect to Agreement system
   - Auto-check permissions
   - Handle denials

3. **KISS binary integration**
   - Use `ProcessServlet.returnBinary()` for services
   - Use `Server.binaryCall()` for clients
   - Handle hybrid JSON+binary protocol

### Phase 6: CLI Tools (Week 4)
1. **koo-cli implementation**
   - `koo dev` - Start development
   - `koo build` - Production build
   - `koo generate` - Component/service generation

### Phase 7: Documentation & Examples (Week 5)
1. **Documentation site**
   - Quick start guide
   - Component documentation
   - API reference

2. **Example domain**
   - Real estate/service scheduling domain
   - Full CRUD implementation
   - Permission setup

---

## 4. KISS Binary Integration

### Server-Side Service Implementation
```java
package koo.services;

import org.kissweb.restServer.ProcessServlet;
import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;

public class HouseService {
    public void getHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        long oid = injson.getLong("oid");
        House house = HouseManager.getByOid(oid);
        
        if (house == null) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", "House not found");
            return;
        }
        
        // Use KISS's native binary support - NO Base64 encoding needed!
        byte[] protoBytes = house.toProto().toByteArray();
        servlet.returnBinary(protoBytes);
    }
}
```

### Client-Side Svelte 5 Integration
```javascript
import { Server } from 'kiss/Server.js';
import { HouseProto } from './generated/HouseProto.js';

async function loadHouse(oid) {
    // Use KISS's binaryCall - automatically handles JSON+binary
    const response = await Server.binaryCall('HouseService', 'getHouse', { oid });
    
    if (!response._Success) {
        throw new Error(response._ErrorMessage);
    }
    
    // response._data is already a Uint8Array from KISS
    return HouseProto.decode(response._data);
}
```

### Hybrid Protocol Benefits
1. **Performance**: Raw binary transmission (no Base64 encoding)
2. **Error Handling**: Standard KISS JSON metadata (`_Success`, `_ErrorMessage`)
3. **Compatibility**: Uses existing KISS infrastructure
4. **Transparency**: Automatic delimiter handling (`003` separator)

---

## 5. Integration Points

### 5.1 Perst Integration
```java
// In CVersion or extension:
public void setProtoData(byte[] protoData) {
    this.protoData = protoData;
}

public byte[] getProtoData() {
    return this.protoData;
}
```

### 5.2 Agreement Permissions
```java
// Map service method to permission:
// searchHouses -> House.read
// createHouse -> House.write
// updateHouse -> House.update
```

### 5.3 Build System Integration
```bash
# Complement ./bld commands:
./bld koo-dev      # Starts koo development server
./bld koo-build    # Builds koo components
./bld koo-generate # Generates components from domain
```

---

## 6. Testing Strategy

### Unit Tests
- Proto schema generation for various domain classes
- Field type mapping (Java → Proto → UI)
- Service method parsing

### Integration Tests
- End-to-end service → UI flow using KISS binaryCall
- Modal interactions
- Permission enforcement

### Test Data
- Sample domain objects
- Mock services
- Test permissions

---

## 7. Configuration

### koo.config.js
```javascript
export default {
  ui: {
    theme: 'light',
    primaryColor: '#1976d2'
  },
  permissions: {
    autoCheck: true
  },
  proto: {
    package: 'com.company.proto'
  }
}
```

---

## 8. Success Metrics

1. **Developer Experience**
   - Can create new domain object and have full CRUD UI in < 5 minutes
   - No manual service writing for basic operations

2. **Performance**
   - ProtoBuf serialization 10x faster than JSON
   - UI loads in < 100ms for typical operations
   - No Base64 encoding overhead

3. **Maintainability**
   - Single source of truth (domain classes)
   - Clear separation of concerns
   - Easy to customize generated components

---

## 9. Key Advantages of KISS Binary Support

### What We Get for Free
- ✅ **Native binary transport** - No custom protocol needed
- ✅ **Hybrid responses** - JSON metadata + binary data
- ✅ **Client-side handling** - `Server.binaryCall()` does the parsing
- ✅ **Error handling** - Standard KISS error metadata
- ✅ **Proven infrastructure** - Battle-tested in production

### Framework Focus Shifts To
- ✅ **Domain modeling** - Pure object design
- ✅ **Service logic** - Business rules
- ✅ **UI generation** - Components from proto schemas
- ✅ **Permission integration** - Agreement mapping

---

## 10. Next Steps

### COMPLETED ✅
1. **Create initial project structure** - DONE (fb06eab7)
2. **Implement ProtoBuf generation** - DONE
3. **Build modal system** - DONE (bad0c11b)
4. **Implement form components** - DONE (13dd77ba)
5. **Implement DataTable & ExpandableRow** - DONE (581fa3cf)
6. **Implement CLI tools** - DONE (fd4fd22d)
7. **Create framework manual** - DONE (649a2c16)
8. **Set up CI/CD pipeline** - PENDING