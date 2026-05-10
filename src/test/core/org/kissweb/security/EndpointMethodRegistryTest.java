package org.kissweb.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EndpointMethodRegistryTest {

    public static class ExternalService {
        public void publicMethod(Object a, Object b, Object c, Object d) {}
        public void _privateMethod(Object a, Object b, Object c, Object d) {}
        @INTERNAL_CALL
        public void internalAnnotatedMethod(Object a, Object b, Object c, Object d) {}
    }

    public static class InternalService {
        public void publicMethod(Object a, Object b, Object c, Object d) {}
        @EXTERNAL_CALL
        public void externalAnnotatedMethod(Object a, Object b, Object c, Object d) {}
    }

    @Test
    public void testExternalPackagePublicMethodIsExposed() {
        EndpointMethodRegistry.clear();
        EndpointMethodRegistry.registerServiceMethods(ExternalService.class);
        
        assertTrue(EndpointMethodRegistry.isExternal("org.kissweb.security.EndpointMethodRegistryTest$ExternalService.publicMethod"),
            "Public method in external package should be exposed by default");
    }

    @Test
    public void testExternalPackageUnderscoreMethodIsInternal() {
        EndpointMethodRegistry.clear();
        EndpointMethodRegistry.registerServiceMethods(ExternalService.class);
        
        assertFalse(EndpointMethodRegistry.isExternal("org.kissweb.security.EndpointMethodRegistryTest$ExternalService._privateMethod"),
            "Underscore method in external package should be internal");
    }

    @Test
    public void testExternalPackageInternalAnnotationMakesMethodInternal() {
        EndpointMethodRegistry.clear();
        EndpointMethodRegistry.registerServiceMethods(ExternalService.class);
        
        assertFalse(EndpointMethodRegistry.isExternal("org.kissweb.security.EndpointMethodRegistryTest$ExternalService.internalAnnotatedMethod"),
            "Method with @INTERNAL_CALL should be internal");
    }

    @Test
    public void testInternalPackageMethodIsInternalByDefault() {
        EndpointMethodRegistry.clear();
        EndpointMethodRegistry.registerServiceMethods(InternalService.class);
        
        assertFalse(EndpointMethodRegistry.isExternal("org.kissweb.security.EndpointMethodRegistryTest$InternalService.publicMethod"),
            "Public method in internal package should be internal by default");
    }

    @Test
    public void testInternalPackageExternalAnnotationMakesMethodExternal() {
        EndpointMethodRegistry.clear();
        EndpointMethodRegistry.registerServiceMethods(InternalService.class);
        
        assertTrue(EndpointMethodRegistry.isExternal("org.kissweb.security.EndpointMethodRegistryTest$InternalService.externalAnnotatedMethod"),
            "Method with @EXTERNAL_CALL in internal package should be exposed");
    }
}