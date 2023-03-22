package io.github.sinri.keel.web.http.fastdocs;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.web.http.fastdocs.page.CataloguePageBuilder;
import io.github.sinri.keel.web.http.fastdocs.page.MarkdownCssBuilder;
import io.github.sinri.keel.web.http.fastdocs.page.MarkdownPageBuilder;
import io.github.sinri.keel.web.http.fastdocs.page.PageBuilderOptions;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @since 1.12
 * @since 3.0.0 TEST PASSED
 */
public class KeelFastDocsKit {
    private final StaticHandler staticHandler;
    private final String rootURLPath;
    private final String rootMarkdownFilePath;

    private String documentSubject = "FastDocs";
    private String footerText = "Public Domain";

    private KeelEventLogger logger;

    /**
     * @param rootURLPath          such as `/prefix/`
     * @param rootMarkdownFilePath such as `path/to/dir/`
     */
    public KeelFastDocsKit(String rootURLPath, String rootMarkdownFilePath) {
        this.staticHandler = StaticHandler.create();
        this.rootURLPath = rootURLPath;
        this.rootMarkdownFilePath = rootMarkdownFilePath;
        this.logger = KeelEventLogger.silentLogger();
    }

    /**
     * If you want to install a route for FastDocs in a certain Router,
     * which mounts URL `[schema]://[domain]/fast-docs/*` to
     * the directory contains markdown files in `resources` as `webroot/markdown/*` .
     *
     * @param router          Router
     * @param urlPathBase     such as `/fast-docs/`
     * @param docsDirPathBase such as `webroot/markdown/`
     */
    public static void installToRouter(
            Router router,
            String urlPathBase,
            String docsDirPathBase,
            String subject,
            String footer,
            KeelEventLogger logger
    ) {
        if (!urlPathBase.endsWith("/")) {
            urlPathBase = urlPathBase + "/";
        }
        if (!docsDirPathBase.endsWith("/")) {
            docsDirPathBase = docsDirPathBase + "/";
        }

        KeelFastDocsKit keelFastDocsKit = new KeelFastDocsKit(urlPathBase, docsDirPathBase)
                .setDocumentSubject(subject)
                .setFooterText(footer)
                .setLogger(logger);

        router.route(urlPathBase + "*")
                .handler(keelFastDocsKit::processRouterRequest);
    }

    public KeelFastDocsKit setLogger(KeelEventLogger logger) {
        this.logger = logger;
        return this;
    }

    public KeelFastDocsKit setDocumentSubject(String documentSubject) {
        this.documentSubject = documentSubject;
        return this;
    }

    public KeelFastDocsKit setFooterText(String footerText) {
        this.footerText = footerText;
        return this;
    }

    public void processRouterRequest(RoutingContext ctx) {
        var requestInfo = new JsonObject()
                .put("method", ctx.request().method().name())
                .put("path", ctx.request().path())
                .put("stream_id", ctx.request().streamId());
        logger.debug(event -> event.message("processRouterRequest start").put("request", requestInfo));
        if (!Objects.equals(ctx.request().method(), HttpMethod.GET)) {
            ctx.response().setStatusCode(405).end();
            logger.warning(event -> event.message("processRouterRequest ends with 405").put("request", requestInfo));
            return;
        }

        String requestPath = ctx.request().path();

        PageBuilderOptions options = new PageBuilderOptions();
        options.logger = logger;
        options.ctx = ctx;
        options.subjectOfDocuments = this.documentSubject;
        options.footerText = this.footerText;
        options.rootURLPath = this.rootURLPath;
        options.rootMarkdownFilePath = this.rootMarkdownFilePath;

        logger.debug("requestPath: " + requestPath);
        if (requestPath.equals(rootURLPath) || requestPath.equals(rootURLPath + "/")) {
            logger.debug(event -> event.message("processRouterRequest -> 302").put("request", requestInfo));
            ctx.redirect(rootURLPath + (rootURLPath.endsWith("/") ? "" : "/") + "index.md");
        } else if (requestPath.endsWith(".md")) {
            logger.debug(event -> event.message("processRouterRequest -> processRequestWithMarkdownPath").put("request", requestInfo));
            processRequestWithMarkdownPath(options);
        } else if (requestPath.equalsIgnoreCase(this.rootURLPath + "catalogue")) {
            logger.debug(event -> event.message("processRouterRequest -> processRequestWithCatalogue").put("request", requestInfo));
            processRequestWithCatalogue(options);
        } else if (requestPath.equalsIgnoreCase(this.rootURLPath + "markdown.css")) {
            logger.debug(event -> event.message("processRouterRequest -> processRequestWithMarkdownCSS").put("request", requestInfo));
            processRequestWithMarkdownCSS(options);
        } else {
            logger.debug(event -> event.message("processRouterRequest -> processRequestWithStaticPath").put("request", requestInfo));
            processRequestWithStaticPath(options);
        }
    }

    private Future<String> getRelativePathOfRequest(RoutingContext ctx) {
        String requestPath = ctx.request().path();

        if (!requestPath.startsWith(this.rootURLPath)) {
            return Future.failedFuture("Not match url root");
        }
        var raw = requestPath.substring(this.rootURLPath.length());
        return Future.succeededFuture(URLDecoder.decode(raw, StandardCharsets.UTF_8));
    }

    protected void processRequestWithMarkdownPath(PageBuilderOptions options) {
        getRelativePathOfRequest(options.ctx)
                .compose(relativePathOfMarkdownFile -> {
                    logger.debug("processRequestWithMarkdownPath relativePathOfMarkdownFile: " + relativePathOfMarkdownFile);
                    String markdownFilePath = this.rootMarkdownFilePath + relativePathOfMarkdownFile;
                    logger.debug("processRequestWithMarkdownPath file: " + markdownFilePath);
                    File x = new File(markdownFilePath);
                    logger.debug("abs: " + x.getAbsolutePath());
                    String markdownContent;
                    try {
                        InputStream resourceAsStream = getClass().getClassLoader()
                                .getResourceAsStream(markdownFilePath);
                        if (resourceAsStream == null) {
                            throw new IOException("resourceAsStream is null");
                        }
                        byte[] bytes = resourceAsStream.readAllBytes();
                        markdownContent = new String(bytes, UTF_8);
                    } catch (IOException e) {
                        logger.exception(e);
                        return Future.failedFuture("Cannot read target file: " + e.getMessage());
                    }

                    options.markdownContent = markdownContent;

                    return Future.succeededFuture(new MarkdownPageBuilder(options));
                })
                .onFailure(throwable -> {
                    logger.exception(throwable, "processRequestWithMarkdownPath 404");
                    options.ctx.response().setStatusCode(404).end();
                })
                .compose(MarkdownPageBuilder::respond)
                .compose(v -> {
                    logger.debug("processRequestWithMarkdownPath ends");
                    return Future.succeededFuture();
                });

    }

    protected void processRequestWithCatalogue(PageBuilderOptions options) {
        options.fromDoc = options.ctx.request().getParam("from_doc");
        new CataloguePageBuilder(options).respond()
                .compose(v -> {
                    logger.debug("processRequestWithCatalogue ends");
                    return Future.succeededFuture();
                });
    }

    protected void processRequestWithMarkdownCSS(PageBuilderOptions options) {
        new MarkdownCssBuilder(options).respond()
                .compose(v -> {
                    logger.debug("processRequestWithMarkdownCSS ends");
                    return Future.succeededFuture();
                });
    }

    protected void processRequestWithStaticPath(PageBuilderOptions options) {
        this.staticHandler.handle(options.ctx);
    }
}
