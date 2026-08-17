package org.kissweb.templates;

import org.kissweb.json.JSONObject;
import org.kissweb.restServer.ProcessServlet;

import java.io.IOException;
import java.util.Map;

/**
 * Server-side helpers for emitting Datastar events from Kiss services and JTE templates.
 *
 * <p>Wraps the raw SSE / HTML primitives in {@link ProcessServlet} with the correct
 * Datastar v1.0.2 wire format, so services never hand-build event strings.</p>
 *
 * <p>Registered with the JTE engine by {@link TemplateProvider}, so JTE templates may
 * declare {@code @param org.kissweb.templates.Datastar datastar} and call these helpers.</p>
 */
public final class Datastar {

    /** Shared instance for templates that receive {@code @param Datastar datastar}. */
    public static final Datastar INSTANCE = new Datastar();

    private Datastar() {
    }

    /**
     * Emit a {@code datastar-patch-signals} SSE event.  Top-level keys of
     * {@code signals} are merged into the client's signal store.
     */
    public static void patchSignals(ProcessServlet servlet, JSONObject signals) throws IOException {
        // Datastar v1.0.2's datastar-patch-signals handler parses the SSE data line as
        // "<field> <value>" (split at the first space).  The field is "signals" and the
        // value is the JSON object to merge.
        servlet.streamSSEEvent("datastar-patch-signals", "signals " + signals.toString());
    }

    /**
     * Convenience overload accepting a plain map of signal values.
     */
    public static void patchSignals(ProcessServlet servlet, Map<String, Object> signals) throws IOException {
        JSONObject jo = new JSONObject();
        for (Map.Entry<String, Object> e : signals.entrySet())
            jo.put(e.getKey(), e.getValue());
        patchSignals(servlet, jo);
    }

    /**
     * Return an HTML fragment that Datastar patches into the DOM (patch-elements),
     * targeted by CSS {@code selector} using the given {@code mode}
     * (outer | inner | remove | replace | prepend | append | before | after).
     */
    public static void patchElements(ProcessServlet servlet, String html, String selector, String mode) {
        servlet.returnHtml(html, selector, mode);
    }

    /**
     * Return an HTML fragment for client-driven swapping (e.g. HTMX hx-target / hx-swap).
     */
    public static void patchElements(ProcessServlet servlet, String html) {
        servlet.returnHtml(html);
    }
}
