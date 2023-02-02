package io.github.sinri.keel.web.http.fastdocs.page;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.io.InputStream;

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
            InputStream resourceAsStream = MarkdownCssBuilder.class.getClassLoader()
                    .getResourceAsStream("web-fastdocs-css/github-markdown.4.0.0.min.css");
            if (resourceAsStream != null) {
                try {
                    cssFileContent = new String(resourceAsStream.readAllBytes());
                } catch (IOException e) {
                    return "";
                }
            } else {
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
