package org.kissweb.templates;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.html.OwaspHtmlTemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.ResourceCodeResolver;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renders JTE templates to HTML strings for hypermedia (HTMX / Datastar) responses.
 *
 * <p>Templates are resolved from the classpath under the {@code /jte} directory
 * (i.e. {@code src/main/precompiled/koo/frontend/jte} copied to {@code WEB-INF/classes/jte} by the build).
 * The engine compiles templates on first use via the JDK compiler.</p>
 *
 * <p>Important: inside Tomcat the webapp {@code ClassLoader} does not expose its
 * JAR URLs, so JTE's runtime Java compiler would fail to resolve {@code gg.jte.*}
 * classes (e.g. {@code gg.jte.html.HtmlTemplateOutput}). To work around this we
 * build an explicit {@link URLClassLoader} over {@code WEB-INF/lib} +
 * {@code WEB-INF/classes} and hand it to the engine as the compile/runtime parent.</p>
 */
public class TemplateProvider {

    private static final TemplateEngine ENGINE = buildEngine();

    private TemplateProvider() {
    }

    public static TemplateProvider get() {
        return new TemplateProvider();
    }

    /**
     * Render a template to an HTML string.
     *
     * @param templateName path of the template relative to /jte, e.g. "fragments/task.jte"
     * @param model        the data model; keys must match the template's {@code @param} names
     * @return the rendered HTML
     */
    public String render(String templateName, Map<String, Object> model) {
        // Expose the Datastar helper to templates that declare
        // @param org.kissweb.templates.Datastar datastar.
        if (!model.containsKey("datastar"))
            model.put("datastar", Datastar.INSTANCE);

        //  With ContentType.Html the generated templates require an HtmlTemplateOutput.
        //  Wrap a StringOutput (the backing buffer) in OwaspHtmlTemplateOutput so the
        //  template renders fully; dynamic @param values are HTML-escaped, static
        //  template text is written verbatim.
        StringOutput buffer = new StringOutput();
        OwaspHtmlTemplateOutput out = new OwaspHtmlTemplateOutput(buffer);
        ENGINE.render(templateName, model, out);
        return buffer.toString();
    }

    private static TemplateEngine buildEngine() {
        try {
            ResourceCodeResolver resolver = new ResourceCodeResolver("jte");

            // Output directory for JTE's compiled template classes.
            Path outputDir = Files.createTempDirectory("jte-compiled");

            // Build an explicit classpath from WEB-INF/lib + WEB-INF/classes so the
            // JDK compiler can resolve gg.jte.* at compile time, and so the compiled
            // template classes can be loaded at runtime.
            List<URL> urls = new ArrayList<>();
            urls.add(outputDir.toUri().toURL());
            Path classesDir = findWebInfClasses();
            if (classesDir != null) {
                urls.add(classesDir.toUri().toURL());
                Path libDir = classesDir.getParent().resolve("lib");
                if (Files.isDirectory(libDir)) {
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(libDir, "*.jar")) {
                        for (Path jar : ds) {
                            urls.add(jar.toUri().toURL());
                        }
                    }
                }
            }

            ClassLoader parent = TemplateProvider.class.getClassLoader();
            URLClassLoader compileLoader = new URLClassLoader(urls.toArray(new URL[0]), parent);

            return TemplateEngine.create(resolver, outputDir, ContentType.Html,
                    compileLoader, "gg.jte.generated.ondemand");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JTE TemplateEngine", e);
        }
    }

    /**
     * Locate {@code WEB-INF/classes} from this class's code source.
     */
    private static Path findWebInfClasses() {
        try {
            URL loc = TemplateProvider.class.getProtectionDomain().getCodeSource().getLocation();
            Path p = Paths.get(loc.toURI());
            if (Files.isDirectory(p)) {
                if (p.endsWith("classes")) {
                    return p;
                }
                Path candidate = p.resolve("classes");
                if (Files.isDirectory(candidate)) {
                    return candidate;
                }
            } else {
                // Running from a jar (e.g. a core jar under WEB-INF/lib).
                Path lib = p.getParent();
                if (lib != null && lib.endsWith("lib")) {
                    Path inf = lib.getParent();
                    if (inf != null) {
                        Path classes = inf.resolve("classes");
                        if (Files.isDirectory(classes)) {
                            return classes;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        return null;
    }
}
