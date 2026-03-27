package oodb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to exclude a field from JSON serialization/deserialization.
 * 
 * Usage:
 * <pre>
 * public class House extends KissCVersion {
 *     private String name;           // Included in JSON
 *     
 *     @JsonIgnore
 *     private String internalNotes;  // Excluded from JSON
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonIgnore {
}
