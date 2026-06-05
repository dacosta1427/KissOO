package org.kissweb.lombok;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for automatic JSON serialization support.
 * Classes with this annotation should implement a toJSON() method
 * or use the JSONSerialization utility to generate JSON.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface toJSON {
    
    /**
     * Fields to explicitly include. If empty, all getters are included.
     */
    String[] includeFields() default {};
    
    /**
     * Fields to explicitly exclude.
     */
    String[] excludeFields() default {};
    
    /**
     * Whether to include null values in the JSON output.
     */
    boolean includeNulls() default false;
}