package oodb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to serialize a referenced CVersion object inline instead of as OID.
 * 
 * By default, OO references (fields whose type extends CVersion) are serialized
 * as their OID (e.g., "owner": 4242). With @JsonIncludeObject, the full object
 * is serialized inline (e.g., "owner": {id: 4242, name: "John", ...}).
 * 
 * Usage:
 * <pre>
 * public class House extends KissCVersion {
 *     private Owner owner;                    // Serialized as: "owner": 4242
 *     
 *     @JsonIncludeObject
 *     private CostProfile costProfile;        // Serialized as: "costProfile": {name: "Premium", ...}
 * }
 * </pre>
 * 
 * Note: Be careful with circular references. If House references Owner and Owner
 * references House, using @JsonIncludeObject on both can cause infinite recursion.
 * Use @JsonIgnore on one side to break the cycle.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonIncludeObject {
}
