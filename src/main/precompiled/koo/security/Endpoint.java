package koo.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Endpoint - Marks a method as a REST endpoint.
 * 
 * Used for bitmap-based authorization. Each @Endpoint is assigned
 * a unique bit position in the EndpointRegistry.
 * 
 * Usage:
 *   @Endpoint(name = "services.CleaningService.getCleaners",
 *             description = "Get all cleaners",
 *             resource = Cleaner.class,
 *             external = true)
 *   def getCleaners(...) { ... }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Endpoint {
    
    /**
     * Unique name for this endpoint
     * Format: "services.ClassName.methodName"
     */
    String name();
    
    /**
     * Human-readable description
     */
    String description() default "";
    
    /**
     * Whether accessible via REST (true) or internal only (false)
     */
    boolean external() default true;
    
    /**
     * Resource class for CRUD permission check
     */
    Class<?> resource() default Object.class;
}