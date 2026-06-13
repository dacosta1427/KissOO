package koo.framework.services;

import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;

public abstract class FrameworkService {
    
    protected JSONObject createSuccessResponse() {
        JSONObject response = new JSONObject();
        response.put("_Success", true);
        return response;
    }
    
    protected JSONObject createErrorResponse(String message) {
        JSONObject response = new JSONObject();
        response.put("_Success", false);
        response.put("_ErrorMessage", message);
        return response;
    }
    
    protected void returnProto(ProcessServlet servlet, byte[] protoData) {
        servlet.returnBinary(protoData);
    }
}