package io.github.sinri.keel.web.fastdocs.page;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.markdown.KeelMarkdownKit;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.12
 */
public class MarkdownPageBuilder implements FastDocsContentResponder {

    private final PageBuilderOptions options;

    public MarkdownPageBuilder(PageBuilderOptions options) {
        this.options = options;
    }

    protected String getPageTitle() {
        return options.subjectOfDocuments
                + " - "
                + URLDecoder.decode(options.ctx.request().path().substring(this.options.rootURLPath.length()), StandardCharsets.UTF_8);
    }

    protected String getLogoDivContent() {
        return options.subjectOfDocuments;
    }

    protected String getComputedBreadcrumbDivContent() {
        String[] components = URLDecoder.decode(
                options.ctx.request().path().substring(this.options.rootURLPath.length()),
                StandardCharsets.UTF_8
        ).split("/");
        List<String> x = new ArrayList<>();
        StringBuilder href = new StringBuilder(this.options.rootURLPath);
        x.add("<a href='" + href + "index.md" + "'>" + options.subjectOfDocuments + "</a>");
        for (var component : components) {
            if (!href.toString().endsWith("/")) {
                href.append("/");
            }
            href.append(component);
            x.add("<a href='" + href + (component.endsWith(".md") ? "" : "/index.md") + "'>" + component + "</a>");
        }
        return Keel.stringHelper().joinStringArray(x, "&nbsp;‣&nbsp;");
    }

    protected String getFooterDivContent() {
        return options.footerText + " <div style=\"display: inline-block;color: gray;\">|</div> Powered by FastDocs";
    }

    private String getCatalogueLink(String fromDoc) {
        return this.options.rootURLPath + "catalogue" + (
                (fromDoc != null && !fromDoc.isEmpty())
                        ? ("?from_doc=" + fromDoc)
                        : ""
        );
    }

    protected String buildPage() {
        KeelMarkdownKit keelMarkdownKit = new KeelMarkdownKit();
        return "<!doctype html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1'>\n" +
                "    <title>" + getPageTitle() + "</title>\n" +
                "    <link rel=\"stylesheet\"\n" +
                "          href=\"" + this.options.rootURLPath + "markdown.css\">\n" +
                "    <style>\n" +
                "        body {\n" +
                "            margin: 0;\n" +
                "            background: white;\n" +
                "        }\n" +
                "\n" +
                "        #header_div {\n" +
                "            background-color: #dddddd;\n" +
                "            padding: 10px;\n" +
                "            height: 30px;\n" +
                "            position: fixed;\n" +
                "                top:0;\n" +
                "                width: 100%;\n" +
                "                line-height: 30px;\n" +
                "            }\n" +
                "            #header_div a:link{\n" +
                "                text-decoration: none;\n" +
                "                color: gray;\n" +
                "            }\n" +
                "            #header_div a:visited{\n" +
                "                text-decoration: none;\n" +
                "                color: gray;\n" +
                "            }\n" +
                "            #header_div a:hover{\n" +
                "                text-decoration: none;\n" +
                "                color: cornflowerblue;\n" +
                "            }\n" +
                "\n" +
                "            #parsed_md_div {\n" +
                "                margin: 50px 10px 50px 300px;\n" +
                "                padding: 10px;\n" +
                "            }\n" +
                "            #footer_div{\n" +
                "                background-color: #dddddd;\n" +
                "                text-align: center;\n" +
                "                padding: 10px;\n" +
                "                height: 30px;\n" +
                "                width: 100%;\n" +
                "                position: fixed;\n" +
                "                bottom: 0;\n" +
                "                line-height: 30px;\n" +
                "            }\n" +
                "            #catalogue_div{\n" +
                "                position: fixed;\n" +
                "                left:0;\n" +
                "                top:50px;\n" +
                "                bottom: 50px;\n" +
                "                width: 300px;\n" +
                "                border-right: 1px solid gray;\n" +
                "            }\n" +
                "            #catalogue_iframe{\n" +
                "                height: 100%;\n" +
                "                width: 300px;\n" +
                "                border:none;\n" +
                "            }\n" +
                "        </style>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <div id=\"header_div\">\n" +
                "            <div style=\"display: inline-block;\">" + getLogoDivContent() + "</div>\n" +
                "            <div style=\"display: inline-block;margin-left:50px;font-size: 10px;line-height: 22px\">" + getComputedBreadcrumbDivContent() + "</div>\n" +
                "        </div>\n" +
                "        <div id='parsed_md_div' class='markdown-body'>\n" +
                "            " + keelMarkdownKit.convertMarkdownToHtml(options.markdownContent) + "\n" +
                "        </div>\n" +
                "        <div id=\"footer_div\">\n" +
                "            <div style=\"display: inline-block;margin: auto 5px;\">" + getFooterDivContent() + "</div>\n" +
                "            <div style=\"display: inline-block;color: gray;\">|</div>\n" +
                "            <div style=\"display: inline-block;margin: auto 5px;\">\n" +
                "                <a href=\"" + getCatalogueLink(null) + "\">Catalogue</a>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        <div id=\"catalogue_div\">\n" +
                "            <iframe id=\"catalogue_iframe\" name=\"catalogue_iframe\"\n" +
                "                    src=\"" + getCatalogueLink(options.ctx.request().path().substring(this.options.rootURLPath.length())) + "\"\n" +
                "            ></iframe>\n" +
                "        </div>\n" +
                "    </body>\n" +
                "</html>";
    }

    @Override
    public void setRoutingContext(RoutingContext ctx) {
        this.options.ctx = ctx;
    }

    @Override
    public Future<Void> respond() {
        return this.options.ctx.response()
                .putHeader("Content-Type", "text/html;charset=UTF-8")
                .end(this.buildPage());
    }
}
