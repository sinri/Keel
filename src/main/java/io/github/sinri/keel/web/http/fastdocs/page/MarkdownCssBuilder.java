package io.github.sinri.keel.web.http.fastdocs.page;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.io.InputStream;
import java.util.Objects;

/**
 * @since 1.12
 */
public class MarkdownCssBuilder implements FastDocsContentResponder {

    private static String cssFileContent = null;
    private final PageBuilderOptions options;

    public MarkdownCssBuilder(PageBuilderOptions options) {
        this.options = options;
    }

    protected String buildPage() {
        if (cssFileContent == null) {
            try (InputStream resourceAsStream = MarkdownCssBuilder.class.getClassLoader()
                    .getResourceAsStream("web-fastdocs-css/github-markdown.4.0.0.min.css")
            ) {
                Objects.requireNonNull(resourceAsStream);
                cssFileContent = new String(resourceAsStream.readAllBytes());
            } catch (Throwable e) {
                return "";
            }
        }
        return cssFileContent;
    }

    @Override
    public void setRoutingContext(RoutingContext ctx) {
        this.options.ctx = ctx;
    }

    @Override
    public Future<Void> respond() {
        return this.options.ctx.response()
                .putHeader("Content-Type", "text/css")
                .end(this.buildPage());
    }
}
