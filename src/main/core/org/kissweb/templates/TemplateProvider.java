package org.kissweb.templates;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.resolve.ResourceCodeResolver;

import java.util.Map;

/**
 * Renders JTE templates to HTML strings for hypermedia (HTMX / Datastar) responses.
 *
 * <p>Templates are resolved from the classpath under the {@code /jte} directory
 * (i.e. {@code src/main/jte} copied to {@code WEB-INF/classes/jte} by the build).
 * The engine compiles templates on first use via the JDK compiler, so the runtime
 * must be a full JDK (true in development). For production, precompile the templates
 * with {@link TemplateEngine#createPrecompiled(java.nio.file.Path, ContentType)}.</p>
 */
public class TemplateProvider {

    private static final TemplateEngine ENGINE =
            TemplateEngine.create(new ResourceCodeResolver("jte"), ContentType.Html);

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
        StringOutput out = new StringOutput();
        ENGINE.render(templateName, model, out);
        return out.toString();
    }
}
