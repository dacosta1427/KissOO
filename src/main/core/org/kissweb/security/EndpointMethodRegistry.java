package org.kissweb.security;

import org.kissweb.database.Connection;
import org.kissweb.json.JSONObject;
import org.kissweb.restServer.ProcessServlet;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EndpointMethodRegistry {
    private static final Map<String, EndpointInfo> registry = new ConcurrentHashMap<>();
    
    public static void register(String fullName, boolean external) {
        registry.put(fullName, new EndpointInfo(external));
    }
    
    public static EndpointInfo get(String fullName) {
        return registry.get(fullName);
    }
    
    public static boolean isExternal(String fullName) {
        EndpointInfo info = registry.get(fullName);
        return info != null && info.external;
    }
    
    public static void registerServiceMethods(Class<?> serviceClass) {
        String className = serviceClass.getName();
        for (Method method : serviceClass.getDeclaredMethods()) {
            if (isServiceMethod(method)) {
                String methodName = method.getName();
                String fullName = className + "." + methodName;
                boolean external = !method.isAnnotationPresent(INTERNAL_CALL.class);
                register(fullName, external);
            }
        }
    }
    
    public static Map<String, EndpointInfo> getAllEndpoints() {
        return new ConcurrentHashMap<>(registry);
    }
    
    public static void clear() {
        registry.clear();
    }
    
    private static boolean isServiceMethod(Method method) {
        if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) return false;
        if (!java.lang.reflect.Modifier.isPublic(method.getDeclaringClass().getModifiers())) return false;
        if (method.getParameterCount() != 4) return false;
        
        Class<?>[] params = method.getParameterTypes();
        return params[0].equals(JSONObject.class)
            && params[1].equals(JSONObject.class)
            && params[2].equals(Connection.class)
            && params[3].getName().contains("ProcessServlet");
    }
    
    public static class EndpointInfo {
        public final boolean external;
        
        public EndpointInfo(boolean external) {
            this.external = external;
        }
    }
}