package services;

import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.templates.Datastar;
import org.kissweb.templates.TemplateProvider;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample service exercising the hypermedia (HTMX / Datastar) support added to the
 * core Kiss framework.  It is not required for normal operation - it exists to
 * demonstrate and validate the returnHtml / SSE paths via the Datastar helper.
 */
public class HypermediaTestService {

    // 1. Datastar JSON signal patch (pure Kiss, zero mods needed)
    public void patchSignals(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        outjson.put("greeting", "Hello from Perst-backed Kiss at " + System.currentTimeMillis());
    }

    // 2. JTE-rendered HTML fragment via returnHtml (HTMX swaps it client-side)
    public void taskFragment(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String title = injson.optString("title", "Sample task");
        Map<String, Object> model = new HashMap<>();
        model.put("title", title);
        servlet.returnHtml(TemplateProvider.get().render("fragments/task.jte", model));
    }

    // 3. Datastar fragment via the JTE-integrated helper (patch-elements)
    public void fragmentDs(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Datastar fragment @ " + LocalTime.now());
        String html = TemplateProvider.get().render("fragments/task.jte", model);
        Datastar.patchElements(servlet, html, "#ds-card", "inner");
    }

    // 4. Datastar-style named SSE stream -> patch-signals (uses the Datastar helper)
    public void liveClock(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            servlet.initializeSSEStream(30_000);
            for (int i = 0; i < 10; i++) {
                JSONObject signals = new JSONObject();
                signals.put("clock", LocalTime.now().toString());
                Datastar.patchSignals(servlet, signals);
                Thread.sleep(1000);
            }
            servlet.endSSEStream();
        } catch (Exception ignored) {
        }
    }

    // 5. Session probe that REQUIRES a valid uuid - proves MOD 4 (X-Kiss-Uuid header).
    //    Returns a Datastar patch-signals event (not plain JSON) so the Auth page's
    //    $whoami / $greeting signals update and the "Check session" button shows feedback.
    public void whoami(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Object perstUser = servlet.getUserData("perstUser");
        boolean auth = perstUser != null;
        JSONObject signals = new JSONObject();
        signals.put("whoami", auth ? "Session valid - you are logged in." : "No valid session.");
        signals.put("greeting", auth ? "Welcome back." : "Please log in.");
        Datastar.patchSignals(servlet, signals);
    }
}
