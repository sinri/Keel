package io.github.sinri.keel.web.http.fastdocs.page;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 1.12
 */
public class PageBuilderOptions {
    public String rootURLPath;
    public String rootMarkdownFilePath;
    public String fromDoc;
    public RoutingContext ctx;
    public String markdownContent;

    public String subjectOfDocuments = "FastDocs";
    public String footerText = "Without Copyright";

    /**
     * @since 3.2.0
     */
    public KeelEventLogger eventLogger;
}
