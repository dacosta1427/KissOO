package koo.framework.proto;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ProtoSchemaGenerator {
    private static final String[] PROTO_TYPES = {
        "String", "int", "long", "double", "float", "boolean",
        "java.lang.String", "java.lang.Integer", "java.lang.Long",
        "java.lang.Double", "java.lang.Float", "java.lang.Boolean"
    };
    
    public ProtoSchema generateSchema(Class<?> domainClass) {
        ProtoSchema schema = new ProtoSchema(domainClass.getSimpleName());
        schema.setPackageName(domainClass.getPackage().getName());
        
        int fieldNumber = 1;
        for (Field field : domainClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (field.isSynthetic()) continue;
            
            ProtoField protoField = createProtoField(field, fieldNumber++);
            schema.addField(protoField);
        }
        
        return schema;
    }
    
    private ProtoField createProtoField(Field javaField, int fieldNumber) {
        ProtoField protoField = new ProtoField();
        protoField.setName(javaField.getName());
        protoField.setNumber(fieldNumber);
        protoField.setType(mapJavaTypeToProto(javaField.getType()));
        
        return protoField;
    }
    
    private String mapJavaTypeToProto(Class<?> javaType) {
        String typeName = javaType.getSimpleName();
        
        if (javaType.isArray()) {
            return "repeated " + mapJavaTypeToProto(javaType.getComponentType());
        }
        
        if (javaType == String.class) {
            return "string";
        } else if (javaType == int.class || javaType == Integer.class) {
            return "int32";
        } else if (javaType == long.class || javaType == Long.class) {
            return "int64";
        } else if (javaType == double.class || javaType == Double.class) {
            return "double";
        } else if (javaType == float.class || javaType == Float.class) {
            return "float";
        } else if (javaType == boolean.class || javaType == Boolean.class) {
            return "bool";
        } else {
            return "bytes"; // Default to bytes for complex types
        }
    }
}