package services;

import domain.actor.owner.Owner;
import koo.core.database.StorageManager;
import koo.services.Login;
import org.garret.perst.continuous.TransactionContainer;
import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.UserData;
import org.kissweb.templates.Datastar;
import org.kissweb.templates.TemplateProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serves the JTE-rendered hypermedia showcase pages (Datastar / HTMX / SSE / Auth / Login / CRUD / Controls).
 * Each full-page method renders a JTE template and returns it via {@code returnHtml}.
 * Interactive fragments (login result, CRUD rows/form) are returned as Datastar patch-elements via
 * {@link Datastar#patchElements(ProcessServlet, String, String, String)}.
 *
 * Convention: Datastar is the default hypermedia lib; HTMX is only used where Datastar genuinely cannot.
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

    // ---------------------------------------------------------------- Login

    public void login(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        servlet.returnHtml(TemplateProvider.get().render("showcase/login.jte", new HashMap<>()));
    }

    /**
     * Fragment endpoint hit by the login form (Datastar @get). Authenticates against Perst,
     * creates a real session, then swaps the card with a welcome fragment whose inline script
     * persists the uuid to localStorage and navigates to the dashboard.
     */
    public void loginPost(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String username = injson.optString("username", "").trim();
        String password = injson.optString("password", "");
        UserData ud = Login.login(db, username, password, outjson, servlet);
        if (ud == null) {
            String msg = htmlEsc(outjson.optString("error", "Invalid username or password"));
            Datastar.patchElements(servlet,
                "<p class='text-red-600 text-sm'>" + msg + "</p>",
                "#login-result", "inner");
            return;
        }
        String uuid = ud.getUuid();
        String welcome =
            "<div class='text-center py-6'>" +
              "<p class='text-emerald-700 font-semibold text-lg'>Welcome, " + htmlEsc(username) + "!</p>" +
              "<p class='text-slate-500 text-sm mt-1'>Taking you to the dashboard&hellip;</p>" +
              "<script type=\"module\">" +
                "localStorage.setItem('kissUuid'," + jsStr(uuid) + ");" +
                "localStorage.setItem('kissUser'," + jsStr(username) + ");" +
                "setTimeout(function(){location.href='/showcase';},700);" +
              "</script>" +
            "</div>";
        Datastar.patchElements(servlet, welcome, "#login-card", "outer");
    }

    // ---------------------------------------------------------------- CRUD (Owners)

    public void crud(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        servlet.returnHtml(TemplateProvider.get().render("showcase/crud.jte", new HashMap<>()));
    }

    public void crudList(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Datastar.patchElements(servlet, ownerRowsHtml(), "#owner-rows", "inner");
    }

    public void crudForm(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String oidStr = injson.optString("oid", "");
        Owner o = null;
        if (!oidStr.isEmpty()) {
            try {
                o = StorageManager.getByOid(Owner.class, Long.parseLong(oidStr));
            } catch (Exception ignore) {
                o = null;
            }
        }
        Datastar.patchElements(servlet, crudFormHtml(o), "#crud-form", "inner");
    }

    public void crudSave(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!StorageManager.isAvailable()) {
            Datastar.patchElements(servlet,
                "<tr><td colspan='4' class='px-3 py-2 text-red-600'>Database unavailable</td></tr>",
                "#owner-rows", "inner");
            return;
        }
        String oidStr = injson.optString("oid", "");
        String name = injson.optString("name", "").trim();
        String email = injson.optString("email", "").trim();
        String phone = injson.optString("phone", "").trim();
        if (name.isEmpty()) {
            Datastar.patchElements(servlet,
                "<p class='text-red-600 text-sm'>Name is required.</p>",
                "#crud-form-status", "inner");
            return;
        }
        try {
            TransactionContainer tc = StorageManager.createContainer();
            Owner o;
            if (!oidStr.isEmpty()) {
                o = StorageManager.getByOid(Owner.class, Long.parseLong(oidStr));
                o.setName(name);
                o.setEmail(email);
                o.setPhone(phone);
                tc.addUpdate(o);
            } else {
                o = new Owner(name, phone, email, true);
                tc.addInsert(o);
            }
            StorageManager.store(tc);
            // Refresh the table; the form stays open so the user can see the saved values.
            Datastar.patchElements(servlet, ownerRowsHtml(), "#owner-rows", "inner");
        } catch (Exception e) {
            Datastar.patchElements(servlet,
                "<p class='text-red-600 text-sm'>Save failed: " + htmlEsc(e.getMessage()) + "</p>",
                "#crud-form-status", "inner");
        }
    }

    public void crudDelete(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String oidStr = injson.optString("oid", "");
        try {
            Owner o = StorageManager.getByOid(Owner.class, Long.parseLong(oidStr));
            TransactionContainer tc = StorageManager.createContainer();
            tc.addDelete(o);
            StorageManager.store(tc);
            Datastar.patchElements(servlet, ownerRowsHtml(), "#owner-rows", "inner");
        } catch (Exception e) {
            Datastar.patchElements(servlet,
                "<p class='text-red-600 text-sm'>Delete failed: " + htmlEsc(e.getMessage()) + "</p>",
                "#crud-form-status", "inner");
        }
    }

    public void crudClear(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Datastar.patchElements(servlet, crudFormPlaceholder(), "#crud-form", "inner");
    }

    // ---------------------------------------------------------------- Controls (Datastar demo)

    public void controls(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        servlet.returnHtml(TemplateProvider.get().render("showcase/controls.jte", new HashMap<>()));
    }

    // ---------------------------------------------------------------- HTML builders

    private String ownerRowsHtml() {
        if (!StorageManager.isAvailable())
            return "<tr><td colspan='4' class='px-3 py-2 text-red-600'>Database unavailable</td></tr>";
        List<Owner> owners = StorageManager.getAll(Owner.class);
        StringBuilder sb = new StringBuilder();
        for (Owner o : owners) {
            long oid = o.getOid();
            sb.append("<tr class='border-t hover:bg-slate-50'>");
            sb.append("<td class='px-3 py-2'>").append(htmlEsc(o.getName())).append("</td>");
            sb.append("<td class='px-3 py-2'>").append(htmlEsc(o.getEmail())).append("</td>");
            sb.append("<td class='px-3 py-2'>").append(htmlEsc(o.getPhone())).append("</td>");
            sb.append("<td class='px-3 py-2 flex gap-3'>");
            sb.append("<button class='text-indigo-600 hover:underline' data-on:click=\"@get('/rest?_class=services/ShowcaseService&amp;_method=crudForm&amp;oid=").append(oid).append("')\">Edit</button>");
            sb.append("<button class='text-red-600 hover:underline' data-on:click=\"@get('/rest?_class=services/ShowcaseService&amp;_method=crudDelete&amp;oid=").append(oid).append("')\">Delete</button>");
            sb.append("</td></tr>");
        }
        if (owners.isEmpty())
            sb.append("<tr><td colspan='4' class='px-3 py-2 text-slate-400'>No owners yet &mdash; click &ldquo;New Owner&rdquo;.</td></tr>");
        return sb.toString();
    }

    private String crudFormHtml(Owner o) {
        boolean edit = o != null;
        String name = edit ? sigEsc(o.getName()) : "";
        String email = edit ? sigEsc(o.getEmail()) : "";
        String phone = edit ? sigEsc(o.getPhone()) : "";
        String oid = edit ? String.valueOf(o.getOid()) : "";
        StringBuilder sb = new StringBuilder();
        sb.append("<form class='bg-slate-50 p-4 rounded border' data-signals=\"{name:'").append(name)
          .append("',email:'").append(email).append("',phone:'").append(phone).append("',oid:'").append(oid).append("'}\"")
          .append(" data-on:submit=\"@get('/rest?_class=services/ShowcaseService&amp;_method=crudSave')\">");
        sb.append("<div class='grid grid-cols-1 sm:grid-cols-3 gap-3'>");
        sb.append("<label class='text-sm flex flex-col gap-1'>Name<input data-bind:name class='border rounded px-2 py-1'></label>");
        sb.append("<label class='text-sm flex flex-col gap-1'>Email<input data-bind:email class='border rounded px-2 py-1'></label>");
        sb.append("<label class='text-sm flex flex-col gap-1'>Phone<input data-bind:phone class='border rounded px-2 py-1'></label>");
        sb.append("</div>");
        sb.append("<div id='crud-form-status' class='mt-2 min-h-[1rem]'></div>");
        sb.append("<div class='mt-3 flex gap-2'>");
        sb.append("<button type='submit' class='px-4 py-1 bg-indigo-600 text-white rounded hover:bg-indigo-700'>Save</button>");
        sb.append("<button type='button' class='px-4 py-1 bg-slate-200 rounded hover:bg-slate-300' data-on:click=\"@get('/rest?_class=services/ShowcaseService&amp;_method=crudClear')\">Cancel</button>");
        sb.append("</div>");
        sb.append("</form>");
        return sb.toString();
    }

    private String crudFormPlaceholder() {
        return "<button class='px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700' " +
               "data-on:click=\"@get('/rest?_class=services/ShowcaseService&amp;_method=crudForm')\">New Owner</button>";
    }

    // ---------------------------------------------------------------- escaping

    private static String htmlEsc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /** Escape for embedding inside a single-quoted JS string in a data-signals attribute. */
    private static String sigEsc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
    }

    /** Wrap a value as a double-quoted JS string literal, escaping quotable chars. */
    private static String jsStr(String s) {
        if (s == null) return "\"\"";
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\' || c == '\n' || c == '\r' || c == '<')
                b.append('\\').append(c);
            else
                b.append(c);
        }
        return b.append("\"").toString();
    }
}
