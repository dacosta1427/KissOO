package services;

import domain.actor.owner.Owner;
import koo.core.database.StorageManager;
import koo.services.Login;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.CVersionHistory;
import org.garret.perst.continuous.TransactionContainer;
import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.UserCache;
import org.kissweb.restServer.UserData;
import org.kissweb.templates.Datastar;
import org.kissweb.templates.TemplateProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Serves the JTE-rendered hypermedia showcase pages (Datastar / HTMX / SSE / Auth / Login / CRUD / Controls).
 * Each full-page method renders a JTE template and returns it via {@code returnHtml}.
 * Interactive fragments (login result, CRUD rows/form) are rendered from JTE fragment templates under
 * {@code showcase/fragments/} and returned as Datastar patch-elements via
 * {@link Datastar#patchElements(ProcessServlet, String, String, String)}.
 *
 * Convention: Datastar is the default hypermedia lib; HTMX is only used where Datastar genuinely cannot.
 */
public class ShowcaseService {

    private static String frag(String name, Map<String, Object> model) {
        // TemplateProvider.render mutates the model (puts "datastar"), so it must be mutable.
        return TemplateProvider.get().render("showcase/fragments/" + name, new HashMap<>(model));
    }

    /**
     * Assemble a full showcase page: shared {@code <head>}, the app-wide nav bar, the
     * page body (rendered from {@code showcase/<name>.jte}), and the footer.
     * The head/nav/footer are rendered as separate templates and concatenated here so we
     * don't depend on JTE {@code @include} (which the on-demand engine does not transform).
     */
    private void page(String name, String title, boolean htmx, ProcessServlet servlet) {
        page(name, title, htmx, servlet, java.util.Map.of());
    }

    private void page(String name, String title, boolean htmx, ProcessServlet servlet, Map<String, Object> extra) {
        String head = TemplateProvider.get().render("showcase/head.jte",
                new HashMap<>(Map.of("title", title, "htmx", htmx)));
        String nav = TemplateProvider.get().render("showcase/navBar.jte",
                new HashMap<>(Map.of("loggedIn", servlet.isLoggedIn())));
        Map<String, Object> bodyModel = new HashMap<>(extra);
        bodyModel.put("loggedIn", servlet.isLoggedIn());
        String body = TemplateProvider.get().render("showcase/" + name + ".jte", bodyModel);
        String footer = TemplateProvider.get().render("showcase/footer.jte", new HashMap<>());
        String html = "<!DOCTYPE html>\n<html lang=\"en\">\n" + head + "\n<body>\n"
                + nav + "\n" + body + "\n" + footer + "\n</body>\n</html>";
        servlet.returnHtml(html);
    }

    public void index(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        page("index", "KissOO Hypermedia Showcase", true, servlet);
    }

    public void datastar(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        page("datastar", "Datastar · KissOO", false, servlet);
    }

    public void htmx(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        page("htmx", "HTMX · KissOO", true, servlet);
    }

    public void sse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        page("sse", "Live SSE · KissOO", false, servlet);
    }

    public void auth(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        page("auth", "Auth · KissOO", false, servlet);
    }

    // ---------------------------------------------------------------- Login

    public void login(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        page("login", "Login · KissOO", false, servlet);
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
            Datastar.patchElements(servlet,
                frag("errorText.jte", Map.of("message", outjson.optString("error", "Invalid username or password"))),
                "#login-result", "inner");
            return;
        }
        String uuid = ud.getUuid();
        servlet.setCookie("kissUuid", uuid, -1);
        Datastar.patchElements(servlet,
            frag("loginWelcome.jte", Map.of("username", username, "uuid", uuid)),
            "#login-card", "outer");
    }

    public void logout(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String uuid = injson.optString("_uuid", "");
        if (!uuid.isEmpty())
            UserCache.removeUser(uuid);
        servlet.setCookie("kissUuid", "", 0);
        servlet.returnHtml("<!DOCTYPE html><html lang=\"en\"><head>"
            + "<meta http-equiv=\"refresh\" content=\"0;url=/showcase/login\"></head>"
            + "<body><p>Logged out. <a href=\"/showcase/login\">Log in</a> again.</p></body></html>");
    }

    // ---------------------------------------------------------------- CRUD (Owners)

    public void crud(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!servlet.isLoggedIn()) {
            servlet.returnHtml("<!DOCTYPE html><html lang=\"en\"><head>"
                + "<meta http-equiv=\"refresh\" content=\"0;url=/showcase/login\"></head>"
                + "<body><p>Please <a href=\"/showcase/login\">log in</a> to manage owners.</p></body></html>");
            return;
        }
        page("crud", "Owners · KissOO", false, servlet);
    }

    public void crudList(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Datastar.patchElements(servlet, frag("ownerRows.jte", Map.of("owners", sortedOwners(injson))), "#owner-rows", "inner");
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
        String signals = crudSignals(o);
        Datastar.patchElements(servlet, frag("crudForm.jte", Map.of("signals", signals)), "#crud-form", "inner");
        // Perst CVersion history of this object, rendered on the Yohn-fork timeline.
        if (o == null) {
            Datastar.patchElements(servlet,
                "<p class=\"muted\">New owner &mdash; no version history yet.</p>", "#owner-history", "inner");
            return;
        }
        Datastar.patchElements(servlet, ownerHistoryFragment(o), "#owner-history", "inner");
    }

    /** Build the Perst CVersion history of an owner as a timeline fragment, including a
     *  per-version field delta (what changed relative to the previous version). */
    private List<Map<String, String>> buildVersions(Owner o) {
        CVersion[] all = o.getVersionHistory().getAllVersions();
        int n = all.length;
        List<Owner> snaps = new ArrayList<>();
        for (CVersion v : all) snaps.add((Owner) v);
        List<Map<String, String>> versions = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) {            // newest (current) first — shown on top
            Owner cur = snaps.get(i);
            int j = n - 1 - i;
            Map<String, String> m = new HashMap<>();
            m.put("date", cur.getDate() == null ? "" : cur.getDate().toString());
            m.put("id", String.valueOf(cur.getId()));
            m.put("side", j % 2 == 0 ? "left" : "right");
            m.put("state", cur.isLastVersion() ? " &middot; current" : (cur.isDeletedVersion() ? " &middot; deleted" : ""));
            m.put("current", cur.isLastVersion() ? "true" : "false");
            m.put("delta", i > 0 ? fieldDelta(snaps.get(i - 1), cur) : "created");
            versions.add(m);
        }
        return versions;
    }

    /** Diff the editable fields of two owner versions into a short human-readable string. */
    private static String fieldDelta(Owner older, Owner newer) {
        List<String> ch = new ArrayList<>();
        if (!Objects.equals(older.getName(), newer.getName()))
            ch.add("name " + nz(older.getName()) + " → " + nz(newer.getName()));
        if (!Objects.equals(older.getEmail(), newer.getEmail()))
            ch.add("email " + nz(older.getEmail()) + " → " + nz(newer.getEmail()));
        if (!Objects.equals(older.getPhone(), newer.getPhone()))
            ch.add("phone " + nz(older.getPhone()) + " → " + nz(newer.getPhone()));
        return ch.isEmpty() ? "no field changes" : String.join("; ", ch);
    }

    private static String nz(String s) { return s == null ? "∅" : s; }

    private String ownerHistoryFragment(Owner o) {
        return frag("ownerHistory.jte", Map.of("versions", buildVersions(o), "name", o.getName()));
    }

    public void crudSave(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!StorageManager.isAvailable()) {
            Datastar.patchElements(servlet,
                frag("errorRow.jte", Map.of("message", "Database unavailable")),
                "#owner-rows", "inner");
            return;
        }
        String oidStr = injson.optString("oid", "");
        String name = injson.optString("name", "").trim();
        String email = injson.optString("email", "").trim();
        String phone = injson.optString("phone", "").trim();
        if (name.isEmpty()) {
            Datastar.patchElements(servlet,
                frag("errorText.jte", Map.of("message", "Name is required.")),
                "#crud-status", "inner");
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
            // Refresh the version-history panel so the newly forked version appears immediately.
            if (!oidStr.isEmpty()) {
                Owner saved = StorageManager.getByOid(Owner.class, Long.parseLong(oidStr));
                Datastar.patchElements(servlet, ownerHistoryFragment(saved), "#owner-history", "inner");
            }
            // Refresh the table; the form stays open so the user can see the saved values.
            Datastar.patchElements(servlet, frag("ownerRows.jte", Map.of("owners", sortedOwners(injson))), "#owner-rows", "inner");
        } catch (Exception e) {
            Datastar.patchElements(servlet,
                frag("errorText.jte", Map.of("message", "Save failed: " + e.getMessage())),
                "#crud-status", "inner");
        }
    }

    public void crudDelete(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String oidStr = injson.optString("oid", "");
        try {
            Owner o = StorageManager.getByOid(Owner.class, Long.parseLong(oidStr));
            TransactionContainer tc = StorageManager.createContainer();
            // Cascade: an owner owns houses, which own bookings, which own schedules.
            // Perst forbids deleting a referenced object, so delete children first.
            java.util.Collection<domain.actor.cleaner.Schedule> allSchedules = StorageManager.getAll(domain.actor.cleaner.Schedule.class);
            for (domain.oov.house.House h : o.getHouses()) {
                for (domain.oov.house.Booking b : h.getBookings()) {
                    long boid = b.getOid();
                    for (domain.actor.cleaner.Schedule s : allSchedules)
                        if (s.getBookingOid() == boid) tc.addDelete(s);
                    tc.addDelete(b);
                }
                tc.addDelete(h);
            }
            tc.addDelete(o);
            StorageManager.store(tc);
            Datastar.patchElements(servlet, frag("ownerRows.jte", Map.of("owners", sortedOwners(injson))), "#owner-rows", "inner");
        } catch (Exception e) {
            Datastar.patchElements(servlet,
                frag("errorText.jte", Map.of("message", "Delete failed: " + e.getMessage())),
                "#crud-status", "inner");
        }
    }

    public void crudClear(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Datastar.patchElements(servlet, frag("crudFormPlaceholder.jte", new HashMap<>()), "#crud-form", "inner");
    }

    // ---------------------------------------------------------------- Controls (Datastar demo)

    public void controls(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        // A real dataset the dynamic table is *constructed* from (in a real app this would be
        // your domain objects, e.g. StorageManager.getAll(Owner.class) — rendered the same way).
        List<List<String>> demoRows = new ArrayList<>();
        demoRows.add(List.of("Ada Lovelace", "Owner", "Active"));
        demoRows.add(List.of("Linus T.", "Cleaner", "Active"));
        demoRows.add(List.of("Grace Hopper", "Owner", "Paused"));
        demoRows.add(List.of("Alan Turing", "Owner", "Active"));
        demoRows.add(List.of("Katherine Johnson", "Cleaner", "Active"));
        page("controls", "Controls · KissOO", false, servlet, Map.of("demoRows", demoRows));
    }

    /** Live, server-side filtered table — proves the search/list pattern against the backend. */
    public void liveSearch(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String q = injson.optString("q", "").trim().toLowerCase();
        List<List<String>> all = demoData();
        List<List<String>> rows = new ArrayList<>();
        for (List<String> r : all)
            if (q.isEmpty() || r.get(0).toLowerCase().contains(q) || r.get(1).toLowerCase().contains(q))
                rows.add(r);
        Datastar.patchElements(servlet, frag("liveRows.jte", Map.of("rows", rows)), "#live-rows", "inner");
    }

    /** Form -> server round-trip — proves the submit/bind pattern against the backend. */
    public void echo(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String name = injson.optString("name", "").trim();
        String email = injson.optString("email", "").trim();
        String html = "<p class=\"success-text\">Server received &mdash; name: <strong>" + esc(name)
                + "</strong>, email: <strong>" + esc(email) + "</strong></p>";
        Datastar.patchElements(servlet, html, "#echo-result", "inner");
    }

    private static List<List<String>> demoData() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Ada Lovelace", "Owner", "Active"));
        rows.add(List.of("Linus T.", "Cleaner", "Active"));
        rows.add(List.of("Grace Hopper", "Owner", "Paused"));
        rows.add(List.of("Alan Turing", "Owner", "Active"));
        rows.add(List.of("Katherine Johnson", "Cleaner", "Active"));
        return rows;
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // ---------------------------------------------------------------- helpers

    private List<Owner> sortedOwners(JSONObject injson) {
        if (!StorageManager.isAvailable()) return new ArrayList<>();
        String sort = injson == null ? "name" : injson.optString("sort", "name");
        String dir = injson == null ? "asc" : injson.optString("dir", "asc");
        List<Owner> owners = new ArrayList<>(StorageManager.getAll(Owner.class));
        Comparator<String> cmp = String.CASE_INSENSITIVE_ORDER;
        owners.sort((a, b) -> cmp.compare(
                sort.equals("email") ? nullToEmpty(a.getEmail())
                        : sort.equals("phone") ? nullToEmpty(a.getPhone())
                        : nullToEmpty(a.getName()),
                sort.equals("email") ? nullToEmpty(b.getEmail())
                        : sort.equals("phone") ? nullToEmpty(b.getPhone())
                        : nullToEmpty(b.getName())));
        if ("desc".equals(dir)) java.util.Collections.reverse(owners);
        return owners;
    }

    /** Build the Datastar data-signals object for the CRUD form (JS-string escaped). */
    private String crudSignals(Owner o) {
        boolean edit = o != null;
        String name = edit ? sigEsc(o.getName()) : "";
        String email = edit ? sigEsc(o.getEmail()) : "";
        String phone = edit ? sigEsc(o.getPhone()) : "";
        String oid = edit ? String.valueOf(o.getOid()) : "";
        return "{name:'" + name + "',email:'" + email + "',phone:'" + phone + "',oid:'" + oid + "'}";
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
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
