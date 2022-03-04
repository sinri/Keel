package io.github.sinri.keel.web.fastdocs.page;

import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.io.File;
import java.net.URL;

/**
 * @since 1.12
 */
public class CataloguePageBuilder implements FastDocsContentResponder {
    private final String rootAbsolutePath;

    private final PageBuilderOptions options;

    public CataloguePageBuilder(PageBuilderOptions options) {
        this.options = options;

        URL x = KeelPropertiesReader.class.getClassLoader().getResource(this.options.rootMarkdownFilePath);
        if (x == null) {
            throw new IllegalArgumentException("rootMarkdownFilePath is not available in File System");
        }
        this.rootAbsolutePath = x.getPath();
    }

    protected String getPageTitle() {
        return options.subjectOfDocuments + " - " + options.ctx.request().path().substring(this.options.rootURLPath.length());
    }

    private boolean isFromDoc() {
        return options.fromDoc != null && !options.fromDoc.isEmpty();
    }

    protected String getLogoDivContent() {
        return options.subjectOfDocuments;
    }

    protected String getCatalogueDivContent() {
        File root = new File(this.rootAbsolutePath);
        StringBuilder code = createHTMLCodeForDir(root, 0);

        return code.toString();
    }

    private StringBuilder createHTMLCodeForDir(File dir, int depth) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class='dir_box'>");

        String boxHref;
        String displayDirName;
        if (depth > 0) {
            boxHref = dir.getAbsolutePath().substring(this.rootAbsolutePath.length());
            displayDirName = dir.getName();
        } else {
            boxHref = ".";
            displayDirName = options.subjectOfDocuments;
        }
        boxHref = boxHref + "/index.md";

        sb.append("<div>");
        sb.append("<div style='display: inline-block;width:20px;border-left: 1px solid lightgrey;'>&nbsp;</div>".repeat(Math.max(0, depth)));
        sb.append("<div class='dir_box_title' style='display: inline-block;'>")
                .append("<span>\uD83D\uDCC1&nbsp;</span>")
                .append("<span>")
                .append("<a href='").append(boxHref).append("' ").append(isFromDoc() ? "target='_parent'" : "").append(" >").append(displayDirName).append("</a>")
                .append("</span>")
                .append("</div>");
        sb.append("</div>");

        File[] files = dir.listFiles(pathname -> {
            if (pathname.isDirectory()) {
                File indexFile = pathname.toPath().resolve("index.md").toFile();
                if (indexFile.exists() && indexFile.isFile()) {
                    return true;
                }
            }
            if (pathname.isFile()) {
                return pathname.getName().endsWith(".md") && !pathname.getName().equalsIgnoreCase("index.md");
            }
            return false;
        });
        if (files != null && files.length > 0) {
            sb.append("<div class='dir_box_body' style='display: inline-block;'>");
            for (var file : files) {
                if (file.isDirectory()) {
                    sb.append(createHTMLCodeForDir(file, depth + 1));
                } else {
                    sb.append("<div class='dir_box_body_item'>")
                            .append("<div style='display: inline-block;width:20px;border-left: 1px solid lightgrey;'>&nbsp;</div>".repeat(Math.max(0, depth + 1)))
                            .append("<span>\uD83D\uDCC4&nbsp;</span>")
                            .append("<span>")
                            .append("<a href='").append(file.getAbsolutePath().substring(this.rootAbsolutePath.length())).append("' ").append(isFromDoc() ? "target='_parent'" : "").append(" >").append(file.getName()).append("</a>")
                            .append("</span>")
                            .append("</div>");
                }
            }
            sb.append("</div>");
        }
        sb.append("</div>");
        return sb;
    }

    protected String getFooterDivContent() {
        return options.footerText + " | Powered by FastDocs";
    }

    protected String buildPage() {
        return "<!doctype html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1'>\n" +
                "    <title>" + getPageTitle() + "</title>\n" +
                "    <!--suppress HtmlUnknownTarget -->\n" +
                "    <link rel=\"stylesheet\" href=\"markdown.css\">\n" +
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
                "            top:0;\n" +
                "            width: 100%;\n" +
                "            line-height: 30px;\n" +
                "            " + (isFromDoc() ? "display: none;" : "") + "\n" +
                "        }\n" +
                "        #header_div a:link{\n" +
                "            text-decoration: none;\n" +
                "            color: gray;\n" +
                "        }\n" +
                "        #header_div a:visited{\n" +
                "            text-decoration: none;\n" +
                "            color: gray;\n" +
                "        }\n" +
                "        #header_div a:hover{\n" +
                "            text-decoration: none;\n" +
                "            color: cornflowerblue;\n" +
                "        }\n" +
                "        #catalogue_div {\n" +
                "            margin: " + (isFromDoc() ? "10px" : "50px 10px 50px") + ";\n" +
                "        }\n" +
                "        #footer_div{\n" +
                "            background-color: #dddddd;\n" +
                "            text-align: center;\n" +
                "            padding: 10px;\n" +
                "            height: 30px;\n" +
                "            width: 100%;\n" +
                "            position: fixed;\n" +
                "            bottom: 0;\n" +
                "            line-height: 30px;\n" +
                "            " + (isFromDoc() ? "display: none;" : "") + "\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"header_div\">\n" +
                "        <div style=\"display: inline-block;\">" + getLogoDivContent() + "</div>\n" +
                "    </div>\n" +
                "    <div id='catalogue_div' class='markdown-body'>\n" +
                "        " + getCatalogueDivContent() + "\n" +
                "    </div>\n" +
                "    <div id=\"footer_div\">\n" +
                "        " + getFooterDivContent() + "\n" +
                "    </div>\n" +
                "    <!--suppress JSUnusedGlobalSymbols -->\n" +
                "    <script lang=\"JavaScript\">\n" +
                "        function locateParentToTargetPage(target) {\n" +
                "            window.parent.window.location = target;\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
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
