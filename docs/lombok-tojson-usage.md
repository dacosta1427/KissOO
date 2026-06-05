# Lombok @toJSON Usage Guide

## Basic Usage

Simply add `@toJSON` to any class with getters:

```java
@Getter @Setter @toJSON
public class User {
    private String name;
    private String email;
    private int age;
}
```

This automatically generates:

```java
public JSONObject toJSON() {
    JSONObject json = new JSONObject();
    json.put("name", getName());
    json.put("email", getEmail());
    json.put("age", getAge());
    return json;
}
```

## Advanced Usage

### Field Exclusion

```java
@Getter @Setter @toJSON
public class User {
    private String name;
    private String email;
    
    @toJSONExclude
    private String password;  // Excluded from JSON
}
```

### Custom Configuration

```java
@Getter @Setter @toJSON(
    includeNulls = true,
    handleCircularReferences = true
)
public class User {
    private String name;
    private String email;
}
```

### Collection Handling

```java
@Getter @Setter @toJSON
public class Group {
    private String name;
    private List<User> members;
    
    // toJSON() automatically handles collections
}
```

## Utility Methods

Use `JSONUtils` for collection serialization:

```java
JSONArray jsonArray = JSONUtils.toJSONArray(users);
JSONObject response = JSONUtils.toCollectionResponse(users);
JSONObject paginated = JSONUtils.toPaginatedResponse(users, 0, 10);
```

## Best Practices

1. Always use `@toJSONExclude` for sensitive data
2. Use `handleCircularReferences = true` for object graphs
3. Use `includeNulls = false` for cleaner JSON (default)
4. Add `toString()` methods for debugging