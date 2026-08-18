# Request for Change — Kiss Core `ProcessServlet`: HyperMedia / SSE Improvements

- **To:** Blake (Kiss framework upstream maintainer)
- **From:** KissOO (David Acosta)
- **Date:** 2026-08-17
- **Status:** Proposal — please fold into upstream so forks don't have to re-apply on re-sync
- **Affected file:** `src/main/core/org/kissweb/restServer/ProcessServlet.java` (true vendored upstream core, overwritten on any Kiss re-sync)

---

## 1. Summary

KissOO added a hypermedia layer on top of Kiss (`org.kissweb.templates`, JTE, Datastar/HTMX).
The hooks for that layer live inside the upstream `ProcessServlet.java`. Two improvements to
that file are below. Both are self-contained in `ProcessServlet` and survive a framework
re-sync only if they are folded into upstream. The `Datastar` *builder* itself (the
`org.kissweb.templates.Datastar` class) is **KissOO-local** and is **not** part of this request
— see §5.

- **Part A — SSE client-disconnect NPE fix** (robustness)
- **Part B — Datastar SSE patch emission** (spec-correct wire format)

---

## 2. Part A — SSE client-disconnect NPE fix

### Why
With an active SSE stream, if the client disconnects (closes the tab) the response's output
buffer is recycled. The standard return paths (`successReturn`/`errorReturn`) still tried to
write / flush / `asyncContext.complete()` the recycled response, throwing:

```
java.lang.NullPointerException: Cannot invoke
"org.apache.catalina.connector.OutputBuffer.isBlocking()" because "this.ob" is null
```

Because it is an NPE (not `IOException`), it escaped every `catch (SQLException | IOException)`
and surfaced as `ERROR [restServer.ProcessServlet]` at the top-level `run()` handler.

### What changed (all in `ProcessServlet.java`)

- New field:
  ```java
  private boolean sseHandled = false;
  ```
- `initializeSSEStream(long)` now registers an `AsyncListener` that flips `sseStreamingMode = false`
  on disconnect / timeout so the async context completes cleanly:
  ```java
  asyncContext.addListener(new jakarta.servlet.AsyncListener() {
      @Override public void onComplete(jakarta.servlet.AsyncEvent event) { }
      @Override public void onTimeout(jakarta.servlet.AsyncEvent event) { sseStreamingMode = false; }
      @Override public void onError(jakarta.servlet.AsyncEvent event) { sseStreamingMode = false; }
      @Override public void onStartAsync(jakarta.servlet.AsyncEvent event) { }
  });
  ```
- `streamSSEText` / `streamSSEEvent` / `streamSSEError` wrap the write+flush in
  `try/catch(Exception)` and, on failure, set `sseStreamingMode = false` (client gone) instead of
  throwing into the caller's loop:
  ```java
  try {
      streamWriter.print("event: " + eventName + '\n');
      for (String line : content.split("\\n", -1))
          streamWriter.print("data: " + line + '\n');
      streamWriter.print('\n');
      streamWriter.flush();
  } catch (Exception e) {
      sseStreamingMode = false;   // client disconnected — stop streaming silently
  }
  ```
- `endSSEStream()` sets `sseHandled = true` **before** the early-return guard, so a disconnect
  (which already flipped `sseStreamingMode`) still suppresses the standard emit:
  ```java
  sseHandled = true;
  if (!sseStreamingMode)
      return;
  ```
- `successReturn(...)` and `errorReturn(...)` skip writing / flushing / completing the response
  when `sseHandled` is true. `errorReturn`'s guard was extended from
  `if (sseStreamingMode)` to `if (sseStreamingMode || sseHandled)`.

---

## 3. Part B — Datastar SSE patch emission (canonical Datastar wire format)

### Why
The original HYPERMEDIA MOD emitted Datastar patches as `text/html` with
`datastar-selector` / `datastar-mode` response headers. That is a supported *fallback*, but it is
**not** the canonical Datastar wire format. Datastar's own client (`datastar.js`) and the reference
Java SDK (`mailq/datastar-java-sdk`) expect a Server-Sent Events stream:
`event: datastar-patch-elements` (and `event: datastar-patch-signals`).

Emitting SSE makes Kiss's hypermedia support spec-correct and interoperable with the official
client/SDK.

### What changed (all in `ProcessServlet.java`)

- New fields:
  ```java
  private Datastar.Event datastarEvent;
  private boolean isDatastarPatch;
  ```
- `returnHtml(String html, String selector, String mode)` (previously the header-based path) now
  builds a Datastar event and queues it:
  ```java
  public void returnHtml(String html, String selector, String mode) {
      emitDatastar(Datastar.patchElements().select(selector).mode(mode).replace(html));
  }
  ```
  Note: the **no-selector** overload `returnHtml(String html)` is intentionally **left unchanged**
  (`isHtmlReturn = true; htmlData = html;`) — it remains `text/html` for HTMX and normal HTML pages.
- New `emitDatastar(Datastar.Event event)` — writes immediately if an SSE stream is already open,
  otherwise buffers it for a one-shot SSE body:
  ```java
  public void emitDatastar(Datastar.Event event) {
      if (sseStreamingMode) {
          try {
              streamSSEEvent(event.name(), event.data());
          } catch (IOException ignored) {
              // client disconnected; streamSSEEvent already cleared the flag
          }
          return;
      }
      this.datastarEvent = event;
      this.isDatastarPatch = true;
  }
  ```
- New `writeDatastarEvent(HttpServletResponse response)` — writes a `text/event-stream` body,
  matching `mailq/datastar-java-sdk`'s `ServletDatastar.build` exactly:
  ```java
  private void writeDatastarEvent(HttpServletResponse response) throws IOException {
      response.setContentType("text/event-stream");
      response.setHeader("Cache-Control", "no-cache");
      response.setHeader("Connection", "keep-alive");
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
      Datastar.Event ev = datastarEvent;
      pw.print("event: " + ev.name() + "\n");
      if (ev.id() != null)
          pw.print("id: " + ev.id() + "\n");
      if (ev.reconnectDelay() != null)
          pw.print("retry: " + ev.reconnectDelay() + "\n");
      for (String line : ev.data().split("\n", -1))
          pw.print("data: " + line + "\n");
      pw.print("\n");
      pw.flush();
      datastarEvent = null;
      isDatastarPatch = false;
  }
  ```
- `successReturn(...)` gains a Datastar branch (when `isDatastarPatch`, emit the queued SSE event;
  the JSON / text-html branches are skipped):
  ```java
  if (!sseHandled) {
      response.setStatus(200);
      if (isDatastarPatch && datastarEvent != null) {
          writeDatastarEvent(response);
      } else if (isHtmlReturn) {
          // ... existing text/html emit ...
      } else if (!isBinaryReturn) {
          // ... existing application/json emit ...
      } else {
          // ... existing application/octet-stream emit ...
      }
      out.flush();
      out.close();
  }
  ```

### Wire-format example (verified on KissOO)
Request: `GET /rest?_class=services/ShowcaseService&_method=crudList`
```
HTTP/1.1 200
Content-Type: text/event-stream;charset=UTF-8
Transfer-Encoding: chunked

event: datastar-patch-elements
data: selector #owner-rows
data: mode inner
data: elements <tr>
data: elements   <td>...</td>
data: elements </tr>
```
`datastar.js` parses this from the response body exactly as it would from a long-lived SSE stream.

The `Datastar.Event` contract used above (builder names + accessors `name()`, `data()`, `id()`,
`reconnectDelay()`) is satisfied by KissOO's local `org.kissweb.templates.Datastar` class, which is
a faithful port of `mailq/datastar-java-sdk`. Alternative equivalent builders are fine upstream.

---

## 4. Impact / non-regression

- **Normal JSON services, auth/login** — untouched. `isDatastarPatch` is only set by the
  Datastar path; otherwise `outjson` serializes exactly as before.
- **HTMX path** — `returnHtml(String html)` (no selector) is unchanged (`text/html`).
- **Long-lived SSE streams** — `emitDatastar` routes to the existing `streamSSEEvent`; the
  `endSSEStream` / Part A fixes still apply.

---

## 5. Change scope — what is and isn't part of this request

| Component | Location | Upstream-core? | Action for Blake |
|---|---|---|---|
| Part A + Part B hooks | `ProcessServlet.java` | **YES** — re-sync overwrites it | **Fold in** |
| `org.kissweb.templates.Datastar` (builder/SSE helper) | `org/kissweb/templates/` | NO — KissOO-local (commit `9c9dc233`) | Not needed; only the `ProcessServlet` hooks are |
| `org.kissweb.templates.TemplateProvider` (injects `Datastar.INSTANCE` into JTE model) | `org/kissweb/templates/` | NO — KissOO-local | Not needed |

So Blake only needs the `ProcessServlet` changes (§2, §3). The `Datastar` builder it calls is our
local helper; upstream may supply any equivalent `Event`/`patchElements` builder.

---

## 6. Verification (done on KissOO)

- `crudList` with selector `#owner-rows` / mode `inner` returns
  `event: datastar-patch-elements` + `data: selector` / `data: mode` / `data: elements …`.
- `loginPost` (admin/admin) returns an SSE patch that swaps `#login-card` (mode `outer`) with the
  welcome `<article>` containing `data-signals`/`data-effect`; `datastar.js` renders it.
- A live SSE clock (`sse.jte` → `HypermediaTestService.liveClock`) still streams correctly.
- HTMX page (`htmx.jte`) and plain JSON services unaffected.

---

## 7. References

- `mailq/datastar-java-sdk` — reference Java SDK; `ServletDatastar.build` is the wire-format source
  of truth for the SSE patch body.
- Datastar protocol: `event: datastar-patch-elements` / `event: datastar-patch-signals`.
- KissOO `AGENTS.md` → "Core Framework Modifications (for Blake / Kiss upstream)" mirrors this RFC.
