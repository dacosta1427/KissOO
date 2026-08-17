package services;

import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.templates.TemplateProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Serves the JTE-rendered hypermedia showcase pages (Datastar / HTMX / SSE / Auth).
 * Each method renders a full HTML document and returns it via {@code returnHtml}.
 * No Svelte, no client-side framework build step - just server-rendered HTML
 * plus hypermedia attributes.
 */
public class ShowcaseService {

    public void index(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        servlet.returnHtml(TemplateProvider.get().render("showcase/index.jte", new HashMap<>()));
    }

    public void datastar(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        servlet.returnHtml(TemplateProvider.get().render("showcase/datastar.jte", new HashMap<>()));
    }

    public void htmx(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        servlet.returnHtml(TemplateProvider.get().render("showcase/htmx.jte", new HashMap<>()));
    }

    public void sse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        servlet.returnHtml(TemplateProvider.get().render("showcase/sse.jte", new HashMap<>()));
    }

    public void auth(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        servlet.returnHtml(TemplateProvider.get().render("showcase/auth.jte", new HashMap<>()));
    }
}
