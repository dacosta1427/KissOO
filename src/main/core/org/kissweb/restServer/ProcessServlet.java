package org.kissweb.restServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kissweb.*;
import org.kissweb.json.JSONException;
import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.templates.Datastar;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.io.OutputStreamWriter;

/**
 * This class processes the incoming REST event queue.
 *
 * Author: Blake McBride
 * Date: 11/26/19
 */
public class ProcessServlet implements Runnable {

    private static final Logger logger = LogManager.getLogger(ProcessServlet.class);

    static final int MaxHold = 600;         // number of seconds to cache microservices before unloading them
    static final int CheckCacheDelay = 60;  // how often to check to unload microservices in seconds
    private ServletContext servletContext;

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final AsyncContext asyncContext;
    private final ServletOutputStream out;
    private UserData ud;
    /** Database connection for the current request. */
    protected Connection DB;
    private byte [] binaryData;
    private boolean isBinaryReturn = false;

    // ========================================================================
    // [HYPERMEDIA MOD 1] HTML return support for JTE / HTMX / Datastar.
    // Mirrors the existing isBinaryReturn pattern: a service method calls
    // returnHtml(...) and successReturn() sends it as text/html instead of
    // serializing outjson.  No other behavior changes.
    // ========================================================================
    private String htmlData = null;
    private boolean isHtmlReturn = false;
    /** A Datastar SSE event queued for a one-shot (non-streaming) response. */
    private java.util.List<Datastar.Event> datastarEvents = new java.util.ArrayList<>();
    private boolean isDatastarPatch = false;
    /** True when streaming mode is active for this request. */
    private volatile boolean sseStreamingMode = false;
    /** True once SSE has taken over the response (endSSEStream completed it). */
    private boolean sseHandled = false;
    /** The PrintWriter for streaming text content. */
    private PrintWriter streamWriter = null;
    private static final ThreadLocal<ProcessServlet> instance = new ThreadLocal<>();
    private JSONObject injson;
    private JSONObject outjson;

    /**
     * Creates a new ProcessServlet.
     *
     * @param packet the packet
     */
    ProcessServlet(org.kissweb.restServer.QueueManager.Packet packet) {
        request = (HttpServletRequest) packet.asyncContext.getRequest();
        response = (HttpServletResponse) packet.asyncContext.getResponse();
        asyncContext = packet.asyncContext;
        out = packet.out;
    }

    /**
     * Called by the executor when a new request is received.
     * This method is called in a separate thread.
     * It catches any exceptions that occur and logs them.
     * It then calls {@link #closeSession()} to release the database connection and
     * any other resources.
     */
    @Override
    public void run() {
        try {
            run2();
        } catch (Throwable e) {
            logger.error(e);
        } finally {
            // Defer cleanup if SSE streaming is ongoing – endSSEStream() will handle it.
            if (!sseStreamingMode) {
                closeSession();
            }
        }
    }

    enum ExecutionReturn {
        Success,
        NotFound,
        Error
    }

    /**
     * Get the absolute path of the root of the back-end application.
     *
     * @return the absolute path of the back-end application root
     */
    public String getRealPath() {
        return servletContext.getRealPath("/");
    }

    /**
     * Returns the ServletContext.
     *
     * @return the ServletContext instance
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Returns the HttpServletRequest.
     *
     * @return the HttpServletRequest instance
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Return the number of files being uploaded.
     *
     * @return the number of files being uploaded
     * @see #getUploadFileName(int)
     * @see #getUploadBufferedInputStream(int)
     * @see #saveUploadFile(int)
     */
    public int getUploadFileCount() {
        int i = 0;
        for ( ; true ; i++) {
            Part filePart = null;
            try {
                filePart = request.getPart("_file-" + i);
            } catch (Exception ignored) {
            }
            if (filePart == null)
                break;
        }
        return i;
    }

    private String getFileName(final Part part) {
        final String partHeader = part.getHeader("content-disposition");
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(
                        content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    /**
     * Returns the name of the file being uploaded.
     *
     * @param i beginning at 0
     * @return the name of the uploaded file, or null if not found
     * @see #getUploadFileCount()
     * @see #getUploadBufferedInputStream(int)
     * @see #saveUploadFile(int)
     */
    public String getUploadFileName(int i) {
        try {
            Part filePart = request.getPart("_file-" + i);
            return getFileName(filePart);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the file extension of a given file upload.
     *
     * @param i the file index starting from 0
     * @return the file extension, or null if not found
     */
    public String getUploadFileType(int i) {
        try {
            final Part filePart = request.getPart("_file-" + i);
            final String fn = getFileName(filePart);
            final int idx = fn.lastIndexOf('.');
            return idx == -1 ? "" : fn.substring(idx + 1);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * In file upload scenarios, this method returns a BufferedInputStream
     * associated with file number i.  When done, the stream must be
     * closed by the application.
     *
     * @param i starting from 0
     * @return a BufferedInputStream for the uploaded file, or null if not found
     * @see BufferedInputStream#close()
     * @see #getUploadFileCount()
     * @see #getUploadFileName(int)
     * @see #saveUploadFile(int)
     */
    public BufferedInputStream getUploadBufferedInputStream(int i) {
        try {
            Part filePart = request.getPart("_file-" + i);
            return new BufferedInputStream(filePart.getInputStream());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * In a file upload scenario, this method returns a byte array of the data that was uploaded.
     *
     * @param i starting from 0
     * @return the uploaded file data as a byte array
     * @throws IOException if an error occurs reading the file
     */
    public byte [] getUploadBytes(int i) throws IOException {
        BufferedInputStream bis = getUploadBufferedInputStream(i);
        if (bis == null)
            return null;
        byte [] ba = FileUtils.readAllBytes(bis);
        bis.close();
        return ba;
    }

    /**
     * Reads upload file "n", saves it to a temporary file, and returns the path to that file.
     *
     * @param n file number
     * @return the absolute path to the saved temporary file
     * @throws IOException if an error occurs during file operations
     *
     * @see #getUploadFileName(int)
     * @see #getUploadBufferedInputStream(int)
     */
    public String saveUploadFile(int n) throws IOException {
        File f = FileUtils.createReportFile("save", "tmp");
        try (
                BufferedInputStream bis = getUploadBufferedInputStream(n);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
        ) {
            int c;
            while (-1 != (c = bis.read()))
                bos.write(c);
        }
        return f.getAbsolutePath();
    }

    /**
     * Reads upload file "n", saves it to the file name passed in, and returns the file name passed in
     *
     * @param n file number
     * @param fileName the name of the file to save the upload to
     * @return the fileName passed in
     * @throws IOException if an error occurs during file operations
     *
     * @see #getUploadFileName(int)
     * @see #getUploadBufferedInputStream(int)
     */
    public String saveUploadFile(int n, String fileName) throws IOException {
        try (
                BufferedInputStream bis = getUploadBufferedInputStream(n);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
        ) {
            int c;
            while (-1 != (c = bis.read()))
                bos.write(c);
        }
        return fileName;
    }

    /**
     * When doing the file-upload method of REST service, everything gets transmitted as strings.
     * I need to convert them back to their correct type.
     * I am using 'S' as a standard to signify string.
     */
    private static Object getObject(HttpServletRequest request, String name) {
        final String value = request.getParameter(name);
        if (value != null) {
            if (!value.isEmpty()) {
                if (name.charAt(0) == '_') // no special processing - all strings
                    return value;
                if (value.charAt(0) == 'S')
                    return value.substring(1);
            }
            if (value.equals("true"))
                return true;
            if (value.equals("false"))
                return false;
            if (value.equals("null"))
                return null;
            if (value.contains("."))
                return Double.parseDouble(value);
            long lnum = Long.parseLong(value);
            if (lnum <= Integer.MAX_VALUE && lnum >= Integer.MIN_VALUE)
                return Integer.parseInt(value);
            else
                return lnum;
        } else
            return null;
    }

    /**
     * This is where the login gets validated and the web service gets processed.
     */
    private void run2() {
        instance.set(this);
        servletContext = request.getServletContext();
        String _className;
        String _method;
        outjson = new JSONObject();
        ProcessServlet.ExecutionReturn res;
        ThreadLevelCache.releaseThreadCaches();  // done in case a thread is re-used (as tomcat does)

        try {
            newDatabaseConnection();
        } catch (Throwable e) {
            errorReturn(response, "Unable to connect to the database", e);
            return;
        }

        _className = request.getParameter("_class");
        String _reqCtype = request.getContentType();
        //  Only treat as a file upload when the request is genuinely multipart/form-data.
        //  A plain GET (or x-www-form-urlencoded POST) that happens to carry a "_class"
        //  query/form parameter must NOT enter this branch, otherwise its string values
        //  get run through getObject()'s numeric parsing and crash (e.g. "Buy milk").
        if (_className != null && _reqCtype != null && _reqCtype.toLowerCase().startsWith("multipart/form-data")) {
            //  is file upload
            _method = request.getParameter("_method");
            logger.info("Enter back-end seeking UPLOAD service " + _className + "." + _method + "()");
            injson = new JSONObject();
            Enumeration<String> names = request.getParameterNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                Object value = getObject(request, name);
                injson.put(name, value);
            }
        } else {
            // ================================================================
            // [HYPERMEDIA MOD 3] External hypermedia clients:
            //  - HTMX GET requests and Datastar GET requests have NO JSON body.
            //  - HTMX POSTs default to application/x-www-form-urlencoded.
            //  - Datastar GETs carry signals URL-encoded in the "datastar" param.
            // ================================================================
            String _ctype = request.getContentType();
            if ("GET".equalsIgnoreCase(request.getMethod())
                    || (_ctype != null && _ctype.startsWith("application/x-www-form-urlencoded"))) {

                injson = new JSONObject();
                Enumeration<String> _names = request.getParameterNames();
                while (_names.hasMoreElements()) {
                    String name = _names.nextElement();
                    injson.put(name, request.getParameter(name));
                }

                // Datastar convention: signals arrive as URL-encoded JSON in
                // the "datastar" query parameter on GET requests.
                String _ds = request.getParameter("datastar");
                if (_ds != null && !_ds.isEmpty()) {
                    try {
                        JSONObject _sig = new JSONObject(
                                java.net.URLDecoder.decode(_ds, StandardCharsets.UTF_8));
                        for (String _key : _sig.keySet())
                            injson.put(_key, _sig.get(_key));
                    } catch (JSONException ignored) {
                        // malformed datastar signals - ignore
                    }
                }

                // Pretty hypermedia routes:
                //   /showcase          -> services/ShowcaseService.index
                //   /showcase/datastar -> services/ShowcaseService.datastar
                if (injson.optString("_class").isEmpty()) {
                    String _uri = request.getRequestURI();
                    if (_uri != null && _uri.startsWith("/showcase")) {
                        String _seg = _uri.substring("/showcase".length()).replaceAll("^/+", "");
                        if (_seg.isEmpty())
                            _seg = "index";
                        injson.put("_class", "services/ShowcaseService");
                        injson.put("_method", _seg);
                    } else {
                        // Optional semantic-URL routing for HTMX/Datastar:
                        // .../services/TaskService/getTaskList
                        //   -> _class = "services/TaskService", _method = "TaskService" / "getTaskList"
                        String _path = request.getPathInfo();
                        if (_path == null || _path.isEmpty())
                            _path = request.getRequestURI();
                        String[] _seg = _path.replaceAll("^/+", "").split("/");
                        if (_seg.length >= 2) {
                            StringBuilder _cls = new StringBuilder();
                            for (int i = 0; i < _seg.length - 1; i++) {
                                if (i > 0) _cls.append('/');
                                _cls.append(_seg[i]);
                            }
                            injson.put("_class", _cls.toString());
                            injson.put("_method", _seg[_seg.length - 1]);
                        }
                    }
                }
                _className = injson.optString("_class", "");
                _method    = injson.optString("_method", "");
                logger.info("Enter back-end seeking HYPERMEDIA service " + _className + "." + _method + "()");
            } else {
            // ================================================================
            // [ORIGINAL KISS CODE - UNCHANGED] JSON body parsing begins here.
            // ================================================================
            String charset = request.getCharacterEncoding();
            if (charset == null || charset.isEmpty())
                charset = "UTF-8";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), charset))) {
                String instr = br.lines().collect(Collectors.joining(System.lineSeparator()));
                injson = new JSONObject(instr);
            } catch (UncheckedIOException uioe) {
                // Handle client abort wrapped in UncheckedIOException
                if (isTomcatClientAbort(uioe)) {
                    logger.debug("Client closed connection - ignoring", uioe);
                    return;
                }
                errorReturn(response, "I/O error reading request body", uioe);
                return;
            } catch (IOException ioe) {
                // Handle direct IOException client aborts
                if (isTomcatClientAbort(ioe)) {
                    logger.debug("Client closed connection - ignoring", ioe);
                    return;
                }
                final String msg = ioe.getMessage();
                if (msg != null && (
                        msg.contains("Stream reset") ||
                                msg.contains("Broken pipe") ||
                                msg.contains("Connection reset"))) {
                    return; // ignore
                }
                errorReturn(response, "I/O error reading request body", ioe);
                return;
            } catch (JSONException je) {
                // Invalid JSON from bots, scanners, or misconfigured clients - log at debug level only
                logger.debug("Invalid JSON request received: " + je.getMessage());
                // Handle response directly to avoid ERROR level logging
                try {
                    if (DB != null) {
                        try {
                            DB.rollback();
                        } catch (SQLException ignored) {
                        }
                    }
                    response.setContentType("application/json");
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.setStatus(200);
                    JSONObject errorJson = new JSONObject();
                    errorJson.put("_Success", false);
                    errorJson.put("_ErrorMessage", "Invalid JSON format");
                    errorJson.put("_ErrorCode", -1);
                    writeJsonText(errorJson.toString());
                    out.flush();
                    out.close();
                    asyncContext.complete();
                } catch (Exception ignored) {
                }
                return;
            } catch (Exception e) {                             // JSON etc.
                errorReturn(response, "Unable to parse request JSON", e);
                return;
            }
            _className = injson.getString("_class");
            _method = injson.getString("_method");
            logger.info("Enter back-end seeking REST service " + _className + "." + _method + "()");
            }
        }

        // ====================================================================
        // [HYPERMEDIA MOD 4] Server.call() injects _uuid automatically; HTMX and
        // Datastar cannot.  Accept the uuid from an HTTP header (set via
        // hx-headers on the HTMX side, or a normal header on the Datastar side)
        // so Kiss authentication works unchanged.  Fall back to "" so that
        // getString("_uuid") never throws for external clients.
        // ====================================================================
        if (injson.optString("_uuid").isEmpty()) {
            String _uuid = request.getHeader("X-Kiss-Uuid");
            // Datastar GET requests cannot easily set a custom header, but they
            // always include the global `uuid` signal in the `datastar` query
            // param (decoded into injson above).  Fall back to it so session
            // lookup works for Datastar clients too.
            if (_uuid == null || _uuid.isEmpty())
                _uuid = injson.optString("uuid");
            // Fall back to the session cookie so full-page navigations (which carry no
            // header or Datastar signal) still resolve the logged-in user server-side.
            if (_uuid == null || _uuid.isEmpty()) {
                jakarta.servlet.http.Cookie[] cookies = request.getCookies();
                if (cookies != null)
                    for (jakarta.servlet.http.Cookie ck : cookies)
                        if ("kissUuid".equals(ck.getName())) { _uuid = ck.getValue(); break; }
            }
            injson.put("_uuid", _uuid != null ? _uuid : "");
        }

        if (_method == null  ||  _method.isEmpty()) {
            errorReturn(response, "missing _method", null);
            return;
        }

        if (_className.isEmpty()) {
            // Core method
            if (_method.equals("LoginRequired")) {
                logger.info("Login is " + (MainServlet.requiresAuthentication() ? "" : "not ") + "required");
                outjson.put("LoginRequired", MainServlet.requiresAuthentication());
                successReturn(response, outjson);
                return;
            } else if (_method.equals("Login")) {
                logger.info("Attempting user login for " + injson.getString("username"));
                try {
                    String uuid = login(injson.getString("username"), injson.getString("password"), outjson);
                    outjson.put("uuid", uuid);
                    successReturn(response, outjson);
                    logger.info("Login successful");
                    return;
                } catch (Exception e) {
                    logger.info("Login failure");
                    loginFailure(response, e);
                    return;
                }
            } else if (_method.equals("Logout")) {
                try {
                    String uuid = injson.getString("_uuid");
                    logger.info("Attempting logout for uuid " + uuid);
                    UserData ud = UserCache.findUser(uuid);
                    checkLogin(ud);  // Validate the user before logging them out
                    UserCache.removeUser(uuid);
                    outjson.put("success", true);
                    successReturn(response, outjson);
                    logger.info("Logout successful");
                    return;
                } catch (Exception e) {
                    logger.info("Logout failure - invalid session");
                    loginFailure(response, e);
                    return;
                }
            } else {
                logger.error("Incorrect internal method call.");
                errorReturn(response, "Incorrect internal method call.", null);
            }
        } else {
            // User defined method
            if (MainServlet.requiresAuthentication()) {
                if (MainServlet.shouldAllowWithoutAuthentication(_className, _method)) {
                    ud = UserCache.findUser(injson.getString("_uuid"));  // in case they are logged in
                    logger.info("Method " + _className + "." + _method + "() allowed without authentication");
                } else {
                    try {
                        logger.info("Validating uuid " + injson.getString("_uuid"));
                        ud = UserCache.findUser(injson.getString("_uuid"));
                        checkLogin(ud);
                    } catch (Exception e) {
                        logger.info("Login failure.");
                        loginFailure(response, e);
                        return;
                    }
                    logger.info("Login success");
                }
            } else {
                ud = UserCache.findUser(injson.getString("_uuid"));
                if (ud == null  &&  !MainServlet.shouldAllowWithoutAuthentication(_className, _method))
                    loginFailure(response, null);
            }
        }

        //  Let a registered application hook prepare the request's connection (e.g. select a
        //  per-tenant schema) now that authentication has completed.  A failure aborts the
        //  request before any service code runs.
        RequestConnectionPreparer preparer = MainServlet.getRequestConnectionPreparer();
        if (preparer != null  &&  DB != null) {
            try {
                preparer.prepare(DB, ud);
            } catch (Exception e) {
                errorReturn(response, "Unable to prepare the database connection for this request", e);
                return;
            }
        }

        res = (new GroovyService()).tryGroovy(this, response, _className, _method, injson, outjson);
        if (res == ProcessServlet.ExecutionReturn.Error)
            return;

        if (res == ProcessServlet.ExecutionReturn.NotFound) {
            res = (new org.kissweb.restServer.JavaService()).tryJava(this, response, _className, _method, injson, outjson);
            if (res == ProcessServlet.ExecutionReturn.Error)
                return;
        }

        if (res == ProcessServlet.ExecutionReturn.NotFound) {
            // Lisp service disabled - requires abcl.jar (see AGENTS.md "Disabling Lisp Services")
            // res = (new LispService()).tryLisp(this, response, _className, _method, injson, outjson);
            // if (res == ProcessServlet.ExecutionReturn.Error)
            //     return;
        }
        if (res == ProcessServlet.ExecutionReturn.NotFound) {
            res = (new CompiledJavaService()).tryCompiledJava(this, response, _className, _method, injson, outjson);
            if (res == ProcessServlet.ExecutionReturn.Error)
                return;
        }

        if (res == ProcessServlet.ExecutionReturn.NotFound) {
            errorReturn(response, "No back-end code found for " + _className, null);
        } else {
            logger.info("REST service " + _className + "." + _method + "() executed successfully");
            successReturn(response, outjson);
        }
    }

    /**
     * Return binary data to the front-end.
     *
     * @param data the binary data to return
     */
    public void returnBinary(byte [] data) {
        isBinaryReturn = true;
        binaryData = data;
    }

    // ========================================================================
    // [HYPERMEDIA MOD 2] Return a rendered HTML fragment (e.g. from JTE) to
    // the front-end.  HTMX will swap it into the DOM; Datastar will patch it
    // using the datastar-selector / datastar-mode headers when provided.
    // ========================================================================

    /**
     * Return an HTML fragment to the front-end (Content-Type text/html).
     *
     * @param html the rendered HTML (typically JTE output)
     */
    public void returnHtml(String html) {
        isHtmlReturn = true;
        htmlData = html;
    }

    /**
     * Return an HTML fragment with Datastar patch instructions.
     *
     * @param html     the rendered HTML
     * @param selector CSS selector of the element(s) Datastar should patch
     * @param mode     one of: outer, inner, remove, replace, prepend, append, before, after
     */
    public void returnHtml(String html, String selector, String mode) {
        emitDatastar(Datastar.patchElements().select(selector).mode(mode).replace(html));
    }

    /**
     * Queue a Datastar {@link Datastar.Event} for emission. If an SSE stream is already open it is
     * written immediately; otherwise it is buffered and written as a one-shot SSE body by
     * {@link #successReturn(HttpServletResponse, JSONObject)}.
     */
    public void emitDatastar(Datastar.Event event) {
        if (sseStreamingMode) {
            try {
                streamSSEEvent(event.name(), event.data());
            } catch (IOException ignored) {
                // client disconnected; streamSSEEvent already cleared the flag
            }
            return;
        }
        this.datastarEvents.add(event);
        this.isDatastarPatch = true;
    }

    /**
     * Set the async timeout for the current request, in milliseconds.  A value of zero or less
     * means no timeout (the request may run to completion however long it takes).
     * <br><br>
     * Call this from a long-running service (for example one that waits on a slow LLM generation)
     * before the work begins.  Without it, the servlet container's default async timeout (typically
     * 30 seconds) applies, and a longer call is severed mid-flight -- which the front-end surfaces as
     * a generic "Error communicating with the server" message.  Normal requests need not call this;
     * they keep the default timeout as a safety net.
     *
     * @param ms the timeout in milliseconds; zero or less means no timeout
     */
    public void setTimeout(long ms) {
        asyncContext.setTimeout(ms);
    }

    /**
     * Initiates streaming mode for this request. Once streaming mode is enabled,
     * the service can send data incrementally to the front-end without buffering
     * the entire response.
     * <br><br>
     * This method must be called before any data is written using the streaming methods.
     * After calling this method, the normal JSON response mechanism is disabled.
     *
     * @param timeoutMs the timeout in milliseconds for the SSE stream
     * @throws IOException if an I/O error occurs while setting up the stream
     */
    public void initializeSSEStream(long timeoutMs) throws IOException {
        if (sseStreamingMode)
            throw new IllegalStateException("SSE Streaming mode is already initialized");

        if (timeoutMs <= 0)
            timeoutMs = 600_000L; // 10-minute default
        asyncContext.setTimeout(timeoutMs);

        // Detect client disconnect / timeout so we stop streaming instead of
        // writing into a recycled response (which throws inside Tomcat).
        asyncContext.addListener(new jakarta.servlet.AsyncListener() {
            @Override public void onComplete(jakarta.servlet.AsyncEvent event) { }
            @Override public void onTimeout(jakarta.servlet.AsyncEvent event) { sseStreamingMode = false; }
            @Override public void onError(jakarta.servlet.AsyncEvent event) { sseStreamingMode = false; }
            @Override public void onStartAsync(jakarta.servlet.AsyncEvent event) { }
        });

        sseStreamingMode = true;

        // Set the response headers
        response.setStatus(200);
        response.setContentType("text/event-stream");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // Wrap the raw ServletOutputStream in a PrintWriter *with* auto-flush enabled – this
        // ensures each println / flush propagates immediately to the socket.
        if (streamWriter == null) {
            streamWriter = new PrintWriter(
                    new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8),
                    true /* autoFlush */);
            streamWriter.println(": connected");
            streamWriter.println();   // blank line required by SSE spec
        }

        response.flushBuffer();
    }

    /**
     * Streams text content to the front-end. This method can be called multiple times
     * to send data incrementally.
     * <br><br>
     * Streaming mode must be initialized first using {@link #initializeSSEStream(long)}.
     * 
     * @param content the text content to stream
     * @throws IOException if an I/O error occurs while writing
     * @throws IllegalStateException if streaming mode is not initialized
     */
    public void streamSSEText(String content) throws IOException {
        if (!sseStreamingMode)
            throw new IllegalStateException("Streaming mode must be initialized first");
        if (streamWriter == null)
            throw new IllegalStateException("Text streaming not available for this content type");
        if (content == null)
            return;
        try {
            for (String line : content.split("\\n", -1))
                streamWriter.print("data: " + line + '\n');
            streamWriter.print('\n');
            streamWriter.flush();
        } catch (Exception e) {
            // Client disconnected (e.g. browser closed the SSE connection).
            // Stop streaming silently instead of throwing into the caller's loop.
            sseStreamingMode = false;
        }
    }

    /**
     * [HYPERMEDIA MOD 6] Stream a *named* SSE event, as required by Datastar
     * (e.g. event name "datastar-patch-signals").  Streaming mode must be
     * initialized first using {@link #initializeSSEStream(long)}.
     *
     * @param eventName the SSE event name
     * @param content   the event payload (single-line JSON or HTML)
     * @throws IOException if an I/O error occurs while writing
     * @throws IllegalStateException if streaming mode is not initialized
     */
    public void streamSSEEvent(String eventName, String content) throws IOException {
        if (!sseStreamingMode)
            throw new IllegalStateException("Streaming mode must be initialized first");
        if (streamWriter == null)
            throw new IllegalStateException("Text streaming not available for this content type");
        if (content == null)
            return;
        try {
            streamWriter.print("event: " + eventName + '\n');
            for (String line : content.split("\\n", -1))
                streamWriter.print("data: " + line + '\n');
            streamWriter.print('\n');
            streamWriter.flush();
        } catch (Exception e) {
            // Client disconnected (e.g. browser closed the SSE connection).
            sseStreamingMode = false;
        }
    }

    /**
     * Streams error content to the front-end. This method can be called multiple times
     * to send data incrementally.
     * <br><br>
     * Streaming mode must be initialized first using {@link #initializeSSEStream(long)}.
     *
     * @param content the error content to stream
     * @throws IOException if an I/O error occurs while writing
     * @throws IllegalStateException if streaming mode is not initialized
     */
    public void streamSSEError(String content) throws IOException {
        if (!sseStreamingMode)
            throw new IllegalStateException("Streaming mode must be initialized first");
        if (streamWriter == null)
            throw new IllegalStateException("Text streaming not available for this content type");
        if (content == null)
            return;
        try {
            for (String line : content.split("\\n", -1))
                streamWriter.print("error: " + line + '\n');
            streamWriter.print('\n');
            streamWriter.flush();
        } catch (Exception e) {
            // Client disconnected (e.g. browser closed the SSE connection).
            sseStreamingMode = false;
        }
    }

    /**
     * Streams a JSON object as a string to the front-end. This is useful for
     * sending structured data in streaming scenarios.
     * <br><br>
     * Streaming mode must be initialized first using {@link #initializeSSEStream(long)}.
     * 
     * @param jsonObject the JSON object to stream
     * @throws IOException if an I/O error occurs while writing  
     * @throws IllegalStateException if streaming mode is not initialized
     */
    public void streamSSEJSON(JSONObject jsonObject) throws IOException {
        streamSSEText(jsonObject.toString());
    }

    /**
     * Completes the streaming response and closes the connection.
     * This method should be called when all streaming data has been sent.
     * <br><br>
     * After calling this method, no more data can be streamed for this request.
     * 
     * @throws IOException if an I/O error occurs while closing
     */
    public void endSSEStream() throws IOException {
        // SSE has taken over the response. Set this BEFORE the early-return below so
        // that even a client-disconnect (which already flipped sseStreamingMode to
        // false) still suppresses the standard response emit in successReturn/errorReturn.
        sseHandled = true;

        // ensure this block runs only once
        if (!sseStreamingMode)
            return;

        // mark streaming finished early to avoid recursion / double-close
        sseStreamingMode = false;

        try {
            if (streamWriter != null) {
                streamWriter.print("data: [DONE]\n\n");
                streamWriter.flush();
                streamWriter.close();
            } else {
                out.flush();
            }
        } finally {
            try {
                asyncContext.complete();
            } catch (Exception e) {
                // ignore
            }
            // perform full cleanup (DB, thread-locals, etc.)
            closeSession();
        }
    }

    /**
     * Checks if streaming mode is currently active for this request.
     * 
     * @return true if streaming mode is active, false otherwise
     */
    public boolean isSseStreamingMode() {
        return sseStreamingMode;
    }

    /**
     * Close the database connection for the current web service request.
     * This does not do a commit or rollback.
     * <br><br>
     * Kiss normally automatically closes the database connection when a web service request ends.
     * This method allows the early closing of the database connection for times when the web service is long-running and the database connection is not needed.
     */
    public void closeConnection() {
        if (DB != null) {
            MainServlet.closeConnection(DB);
            DB = null;
        }
    }

    /**
     * Close the database connection for the current web service request.
     * Any uncommitted database operations will be rolled back if success is false.
     * <br><br>
     * Kiss normally automatically closes the database connection when a web service request ends.
     * This method allows the early closing of the database connection for times when the web service is long-running and the database connection is not needed.
     *
     * @param success if true, any uncommitted database operations will be committed; otherwise they will be rolled back
     */
    public void closeConnection(boolean success) {
        if (DB != null) {
            MainServlet.closeConnection(DB, success);
            DB = null;
        }
    }

    private void writeJsonText(String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Write a queued Datastar {@link Datastar.Event} as a one-shot SSE response, matching the
     * wire format produced by mailq/datastar-java-sdk's ServletDatastar (event: / data: lines,
     * terminated by a blank line). datastar.js parses this from the response body.
     */
    private void writeDatastarEvent(HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        for (Datastar.Event ev : datastarEvents) {
            pw.print("event: " + ev.name() + "\n");
            if (ev.id() != null)
                pw.print("id: " + ev.id() + "\n");
            if (ev.reconnectDelay() != null)
                pw.print("retry: " + ev.reconnectDelay() + "\n");
            for (String line : ev.data().split("\n", -1))
                pw.print("data: " + line + "\n");
            pw.print("\n");
        }
        pw.flush();
        datastarEvents.clear();
        isDatastarPatch = false;
    }

    /**
     * Returns a successful response to the front-end.
     *
     * If the response has already been generated elsewhere, this does nothing.
     * If the database is open, it is committed.
     * The response is marked as successful and the error message is removed.
     * The response code is set to 200, which is the same as a successful response.
     * If a binary response was requested, the binary data is sent.  Otherwise, the JSON data is sent.
     * @param response the HTTP response to send the success back on
     * @param outjson the JSON data to send
     */
    private void successReturn(HttpServletResponse response, JSONObject outjson) {
        if (sseStreamingMode) {
            return;          // streaming mode active, response handled elsewhere
        }
        try {
            if (DB != null)
                DB.commit();
            outjson.put("_Success", true);
            outjson.put("_ErrorCode", 0);  // success
            outjson.put("_BootId", MainServlet.getBootId());
                if (!sseHandled) {
                    response.setStatus(200);
                    if (isDatastarPatch && !datastarEvents.isEmpty()) {
                        writeDatastarEvent(response);
                    } else if (isHtmlReturn) {
                    response.setContentType("text/html");
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    if (htmlData != null) {
                        //  The async ServletOutputStream performs *non-blocking* writes, so a
                        //  single large out.write() can be truncated (only what fits the socket
                        //  window is sent before close()/complete() race it).  Emit the document
                        //  in small flushed chunks through a PrintWriter - exactly the pattern the
                        //  SSE streamer uses, which is known to deliver the full body.
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
                        for (String line : htmlData.split("\n", -1))
                            pw.println(line);
                        pw.flush();
                        htmlData = null;
                        isHtmlReturn = false;
                    }
                } else if (!isBinaryReturn) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    writeJsonText(outjson.toString());
                } else {
                    response.setContentType("application/octet-stream");
                    writeJsonText(outjson.toString() + "\003");
                    if (binaryData != null) {
                        out.write(binaryData);
                        binaryData = null;
                        isBinaryReturn = false;
                    }
                }
                out.flush();
                out.close();     // this causes the second response
            }
        } catch (SQLException | IOException ignored) {
        } finally {
            if (!sseHandled) {
                try {
                    asyncContext.complete();
                } catch (IllegalStateException ignore) {
                    // The request may have already been completed by a streaming method.
                }
            }
            // Note: closeSession() is now handled in the outer run() finally block
        }
    }

    private static final AtomicInteger errorNumber = new AtomicInteger(0);

    /**
     * Returns an error response to the front-end.
     * If the response has already been generated elsewhere, this does nothing.
     * If the database is open, it is rolled back.
     * The response is marked as unsuccessful and the error message is sent.
     * The response code is set to 200, which is the same as a successful response.
     * @param response the HTTP response to send the error back on
     * @param msg the error message to send
     * @param e the exception that caused the error, or null if none
     */
    void errorReturn(HttpServletResponse response, String msg, Throwable e) {
        //  A service that ran without authentication can signal it needs a logged-in user
        //  by throwing LoginRequiredException (via requireLogin()).  Route that to the
        //  standard "not logged in" response (_ErrorCode = 2) regardless of which service
        //  runner caught and forwarded it here.
        if (containsLoginRequired(e)) {
            loginFailure(response, e);
            return;
        }
        int errorCode;
        if (e instanceof ServerException)
            errorCode = ((ServerException) e).getErrorCode();
        else if (e instanceof UserException)
            errorCode = ((UserException) e).getErrorCode();
        else if (e instanceof LogException)
            errorCode = ((LogException) e).getErrorCode();
        else
            errorCode = -1;
        if (sseStreamingMode || sseHandled) {
            return;          // streaming mode active or SSE already completed the response
        }
        try {
            if (DB != null) {
                try {
                    DB.rollback();
                } catch (SQLException ignored) {
                }
            }
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setStatus(200);
            JSONObject outjson = new JSONObject();
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", msg != null ? msg :(e != null ? e.getMessage() : "unspecified"));
            outjson.put("_ErrorCode", errorCode);
            if (!(e instanceof UserException))
                log_error(msg, e);
            writeJsonText(outjson.toString());
            out.flush();
            out.close();  //  this causes the second response
        } catch (Exception ignored) {
        } finally {
            try {
                asyncContext.complete();
            } catch (IllegalStateException ignore) {
                // The request may have already been completed by a streaming method.
            }
            // Note: closeSession() is now handled in the outer run() finally block
        }
    }

    private void loginFailure(HttpServletResponse response, Throwable e) {
        String msg = null;
        if (e != null) {
            msg = e.getMessage();
            if (msg == null) {
                Throwable t = e.getCause();
                if (t != null)
                    msg = t.getMessage();
            }
        }
        if (msg == null)
            msg = "Login failure.";
        if (DB != null) {
            try {
                DB.rollback();
            } catch (SQLException ignored) {
            }
        }
        // Note: closeSession() is now handled in the outer run() finally block
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(200);
        JSONObject outjson = new JSONObject();
        outjson.put("_Success", false);
        outjson.put("_ErrorMessage", msg);
        outjson.put("_ErrorCode", 2);  // login failure
        outjson.put("_BootId", MainServlet.getBootId());
        try {
            writeJsonText(outjson.toString());
            out.flush();
            out.close();  //  this causes the second response
        } catch (IOException ignore) {
        }
        try {
            asyncContext.complete();
        } catch (IllegalStateException ignore) {
            // The request may have already been completed by a streaming method.
        }
    }

    /**
     * Determine if a string is empty.
     *
     * @param str the string to check
     * @return true if the string is empty, false otherwise
     */
    boolean isEmpty(final String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Get all user data.
     *
     * @return the UserData instance for the current user, or null if not logged in
     */
    public UserData getUserData() {
        return ud;
    }

    /**
     * Get a specific user data element.
     *
     * @param key the key for the user data element
     * @return the user data element, or null if not found (or not logged in)
     */
    public Object getUserData(String key) {
        return ud == null ? null : ud.getUserData(key);
    }

    /**
     * Whether the current request is from a logged-in user.
     * <br><br>
     * Useful in a service registered with
     * {@link MainServlet#allowWithoutAuthentication(String, String)}, which runs whether
     * or not the caller is authenticated: branch on this to do one thing when logged in
     * and another when not.
     *
     * @return true if a valid user session is associated with this request
     *
     * @see #requireLogin()
     */
    public boolean isLoggedIn() {
        return ud != null;
    }

    /**
     * Set a browser cookie (path "/").  Used to carry the session uuid across full-page
     * navigations (which do not send the {@code X-Kiss-Uuid} header or the Datastar
     * {@code uuid} signal), so {@link #isLoggedIn()} and nav rendering see the session.
     *
     * @param name the cookie name
     * @param value the cookie value (use "" with maxAge 0 to clear)
     * @param maxAgeSeconds max age in seconds; -1 for a browser-session cookie
     */
    public void setCookie(String name, String value, int maxAgeSeconds) {
        jakarta.servlet.http.Cookie c = new jakarta.servlet.http.Cookie(name, value);
        c.setPath("/");
        c.setMaxAge(maxAgeSeconds);
        response.addCookie(c);
    }

    /**
     * Require a logged-in user for the remainder of this service call.
     * <br><br>
     * Intended for a service registered with
     * {@link MainServlet#allowWithoutAuthentication(String, String)} that, after some
     * unauthenticated work, decides it needs an authenticated user.  When no user is
     * logged in this aborts the call with the standard "not logged in" response
     * (<code>_ErrorCode = 2</code>), the same response a normally-protected method
     * produces, so the front-end routes the user to login.
     *
     * @see #isLoggedIn()
     */
    public void requireLogin() {
        if (ud == null)
            throw new LoginRequiredException("Login required.");
    }

    /**
     * True if a LoginRequiredException appears anywhere in the cause chain.  Service
     * runners wrap the thrown exception (e.g. in InvocationTargetException) and forward
     * it to errorReturn, so the chain is walked rather than checking the top type.
     */
    private static boolean containsLoginRequired(Throwable e) {
        while (e != null) {
            if (e instanceof LoginRequiredException)
                return true;
            e = e.getCause();
        }
        return false;
    }

    /**
     * Returns the IP address of the client.
     *
     * @return the client's IP address
     */
    public String getRemoteAddr() {
        String remoteAddr = "";

        if (request != null) {
            try {
                remoteAddr = request.getHeader("X-FORWARDED-FOR");
                if (remoteAddr == null || "".equals(remoteAddr))
                    remoteAddr = request.getRemoteAddr();
            } catch (IllegalStateException ignored) {
                // Request object has been recycled (e.g. client disconnected during HTTP/2 stream)
            }
        }
        return remoteAddr;
    }

    /**
     * Get the current instance of the ProcessServlet.
     * This allows us a global way of getting the ProcessServlet instance associated with a particular thread / request.
     *
     * @return  the current instance
     */
    public static ProcessServlet getInstance() {
        return instance.get();
    }

    /**
     * Get the current connection to the database associated with this request.
     * This may be null if the database is not available.
     *
     * @return the current connection
     */
    public static Connection getConnection() {
        return instance.get().DB;
    }

    /**
     * Returns the input JSON object associated with this request.
     *
     * @return the JSON object
     */
    public static JSONObject getInjson() {
        return instance.get().injson;
    }

    /**
     * Returns the output JSON object associated with this request.
     * This is the object where the web service should place any data it wants to send back to the front-end.
     *
     * @return the JSON object
     */
    public static JSONObject getOutjson() {
        return instance.get().outjson;
    }

    private void log_error(final String str, final Throwable e) {
        if (e instanceof UserException)
            return;  //  no log
        if (e instanceof LogException)
            logger.warn(str + " " + e.getMessage());
        else
            logger.error(str, e);
    }

    private String login(String user, String password, JSONObject outjson) throws Exception {
        UserData ud = null;
        if (MainServlet.requiresAuthentication()) {
            try {
                ud = (UserData) GroovyClass.invoke(true, "Login", "login", null, DB, user, password, outjson, this);
            } catch (InvocationTargetException e) {
                logger.error("Login error", e.getTargetException());
            } catch (Exception e) {
                logger.error(e);
            }
            if (ud == null)
                throw new LogException("Invalid login.");
        } else
            ud = UserCache.newUser(user, password, null);
        return ud.getUuid();
    }

    private void checkLogin(UserData ud) throws Exception {
        if (ud == null)
            throw new UserException("You have been logged out due to inactivity. Please log in again.");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeout = ud.getLastAccessDate().plusSeconds(120);  // cache user data for 120 seconds
        if (MainServlet.requiresAuthentication() && now.isAfter(timeout)) {
            Boolean good = (Boolean) GroovyClass.invoke(true, "Login", "checkLogin", null, DB, ud, this);
            if (!good) {
                UserCache.removeUser(ud.getUuid());
                throw new LogException("Invalid login.");
            }
        }
        ud.setLastAccessDate(LocalDateTime.now());
    }

    private void newDatabaseConnection() throws SQLException {
        // NonSqlConnection support (e.g., Perst OODBMS). When a non-SQL connection is
        // registered via MainServlet.putEnvironment("NonSqlConnection", conn), it is used
        // in preference to (or instead of) a SQL database.
        Object nonSqlConn = MainServlet.getEnvironment("NonSqlConnection");
        if (nonSqlConn instanceof Connection) {
            DB = (Connection) nonSqlConn;
            logger.info("Using NonSqlConnection (e.g. Perst) for database operations");
            return;
        }
        if (!MainServlet.hasDatabase())
            return;
        logger.info("Pool status - busy: " + MainServlet.getCpds().getNumBusyConnections() + 
                   ", idle: " + MainServlet.getCpds().getNumIdleConnections());
        final java.sql.Connection conn = MainServlet.getCpds().getConnection();
        conn.setAutoCommit(false);  //  all SQL operations require a commit but Kiss does a commit at the end of each service
        DB = new Connection(conn);
        String databaseSchema = (String) MainServlet.getEnvironment("DatabaseSchema");
        if (databaseSchema != null  &&  !databaseSchema.isEmpty())
            DB.setSchema(databaseSchema);
        logger.info("New database connection obtained - pool now busy: " + 
                   MainServlet.getCpds().getNumBusyConnections());
    }

    private void closeSession() {
        instance.remove();
        
        // Clean up streaming resources
        if (sseStreamingMode && streamWriter != null) {
            try {
                streamWriter.close();
            } catch (Exception e) {
                logger.warn("Error closing stream writer", e);
            }
            streamWriter = null;
        }
        sseStreamingMode = false;
        
        // Clean up database connection
        java.sql.Connection sconn = null;
        try {
            if (DB != null) {
                sconn = DB.getSQLConnection();
                DB.close();
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            DB = null;
        }
        try {
            if (sconn != null)
                sconn.close();
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    /**
     * I want to ignore client disconnects that were caused by the client having a bad Internet connection.
     *
     * @param t
     * @return
     */
    private static boolean isTomcatClientAbort(Throwable t) {
        while (t != null) {
            if ("org.apache.catalina.connector.ClientAbortException".equals(t.getClass().getName()))
                return true;
            t = t.getCause();
        }
        return false;
    }

}
