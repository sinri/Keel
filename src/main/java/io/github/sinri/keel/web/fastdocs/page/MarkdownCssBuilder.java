package io.github.sinri.keel.web.fastdocs.page;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * @since 1.12
 */
public class MarkdownCssBuilder implements FastDocsContentResponder {

    private final PageBuilderOptions options;

    public MarkdownCssBuilder(PageBuilderOptions options) {
        this.options = options;
    }


    protected String buildPage() {
        InputStream resourceAsStream = MarkdownCssBuilder.class.getClassLoader()
                .getResourceAsStream("web-fastdocs-css/github-markdown.4.0.0.min.css");
        if (resourceAsStream != null) {
            try {
                return new String(resourceAsStream.readAllBytes());
            } catch (IOException e) {
                return "";
            }
        }
        return "";
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
