package services;

import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.templates.TemplateProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample service exercising the hypermedia (HTMX / Datastar) support added to the
 * core Kiss framework.  It is not required for normal operation - it exists to
 * demonstrate and validate the new returnHtml / SSE paths (MODs 1, 2, 5, 6).
 */
public class HypermediaTestService {

    // 1. Datastar JSON signal patch (pure Kiss, zero mods needed)
    public void patchSignals(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        outjson.put("greeting", "Hello from Perst-backed Kiss at " + System.currentTimeMillis());
    }

    // 2. JTE-rendered HTML fragment via returnHtml (MODs 1, 2, 5)
    public void taskFragment(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String title = injson.optString("title", "Sample task");
        Map<String, Object> model = new HashMap<>();
        model.put("title", title);
        // Datastar headers instruct the client how to patch the DOM
        servlet.returnHtml(
                TemplateProvider.get().render("fragments/task.jte", model),
                "#task-list", "append");
    }

    // 3. Datastar-style named SSE stream (existing initializeSSEStream + MOD 6)
    public void liveClock(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            servlet.initializeSSEStream(30_000);
            for (int i = 0; i < 10; i++) {
                servlet.streamSSEEvent("datastar-patch-signals",
                        "{\"clock\": \"" + java.time.LocalTime.now() + "\"}");
                Thread.sleep(1000);
            }
            servlet.endSSEStream();
        } catch (Exception ignored) {
        }
    }
}
