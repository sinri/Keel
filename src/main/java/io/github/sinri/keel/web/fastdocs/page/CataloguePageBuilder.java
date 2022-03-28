package io.github.sinri.keel.web.fastdocs.page;

import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;

public class CataloguePageBuilder implements FastDocsContentResponder {
    private final PageBuilderOptions options;
    private final boolean embedded;
    private final String actualFileRootOutsideJAR;

    private static String catalogueDivContentCache = null;

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

    public CataloguePageBuilder(PageBuilderOptions options) {
        this.options = options;

        URL x = KeelPropertiesReader.class.getClassLoader().getResource(this.options.rootMarkdownFilePath);
        if (x == null) {
            throw new IllegalArgumentException("rootMarkdownFilePath is not available in File System");
        }
        this.embedded = x.toString().contains("!/");
        this.actualFileRootOutsideJAR = x.getPath();
        options.logger.debug("EMBEDDED: " + embedded + " url: " + x + " actualFileRootOutsideJAR: " + actualFileRootOutsideJAR);
    }

    private boolean isFromDoc() {
        return options.fromDoc != null && !options.fromDoc.isEmpty();
    }

    protected String getLogoDivContent() {
        return options.subjectOfDocuments;
    }

    protected String getPageTitle() {
        return options.subjectOfDocuments
                + " - " +
                URLDecoder.decode(options.ctx.request().path().substring(this.options.rootURLPath.length()), StandardCharsets.UTF_8);
    }

    protected String getCatalogueDivContent() {
        if (catalogueDivContentCache == null) {
            if (embedded) {
                catalogueDivContentCache = createHTMLCodeForDir(buildTreeInsideJAR()).toString();
            } else {
                catalogueDivContentCache = createHTMLCodeForDir(buildTreeOutsideJAR()).toString();
            }
        }
        return catalogueDivContentCache;
    }

    protected String getFooterDivContent() {
        return options.footerText + " | Powered by FastDocs";
    }

    public StringBuilder createHTMLCodeForDir(TreeNode tree) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class='dir_box'>");

        String boxHref;
        String displayDirName;
        boxHref = tree.href;
        if (tree.level > 0) {
            displayDirName = tree.name;
        } else {
            displayDirName = options.subjectOfDocuments;
        }
        //boxHref = boxHref + "/index.md";

        sb.append("<div class='dir_box_body_item' style='display: inline-flex'>");
        sb.append("<div style='display: inline-block;width:20px;border-left: 1px solid lightgrey;'>&nbsp;</div>".repeat(Math.max(0, tree.level)));
        sb.append("<div class='dir_box_title' style='display: inline-block;'>")
//                .append("<span>")
//                .append("\uD83D\uDCC1&nbsp;")
//                .append("</span>")
//                .append("<span>")
                .append("<a href='").append(boxHref).append("' ").append(isFromDoc() ? "target='_parent'" : "").append(" style='white-space: nowrap;display: inline-block;'").append(" >").append("\uD83D\uDCC1&nbsp;").append(displayDirName).append("</a>")
//                .append("</span>")
                .append("</div>");
        sb.append("</div>");

        // DIRS start
        if (tree.href.endsWith("/index.md")) {
            // as dir
            for (var child : tree.getSortedChildren()) {
                if (child.href.endsWith("/index.md")) {
                    sb.append(createHTMLCodeForDir(child));
                } else {
                    sb.append("<div class='dir_box_body_item' style='display: inline-flex'>");
                    sb.append("<div style='display: inline-block;width:20px;border-left: 1px solid lightgrey;'>&nbsp;</div>".repeat(Math.max(0, tree.level + 1)));
                    //                .append("<span>")
                    //                            .append("\uD83D\uDCC4&nbsp;")
                    //                .append("</span>")
                    //                .append("<span>")
                    sb
                            //                .append("<span>")
//                            .append("\uD83D\uDCC4&nbsp;")
//                .append("</span>")
//                .append("<span>")
                            .append("<a href='")
                            .append(child.href)
                            .append("' ")
                            .append(isFromDoc() ? "target='_parent'" : "")
                            .append(" style='white-space: nowrap;display: inline-block;'")
                            .append(" >").append("\uD83D\uDCC4&nbsp;").append(child.name)
                            .append("</a>")
//                            .append("</span>")
                            .append("</div>");
                }
            }
        }
        // DIRS end

        sb.append("</div>");
        return sb;
    }

    protected TreeNode buildTreeInsideJAR() {
        TreeNode tree = new TreeNode();
        tree.href = options.rootURLPath + "index.md";
        tree.level = 0;
        tree.name = options.subjectOfDocuments;
        List<JarEntry> jarEntries = KeelHelper.traversalInJar(options.rootMarkdownFilePath);
        for (var jarEntry : jarEntries) {
            TreeNode child = buildTreeNodeInJar(jarEntry);
            if (child != null) {
                tree.addChild(child);
            }
        }
        options.logger.debug("TREE", tree.toJsonObject());
        return tree;
    }

    private TreeNode buildTreeNodeInJar(JarEntry jarEntry) {
        options.logger.debug("buildTreeNodeInJar: " + jarEntry.getName() + " isDir: " + jarEntry.isDirectory());
        TreeNode treeNode = new TreeNode();
        treeNode.name = String.valueOf(Path.of(jarEntry.getName()).getFileName());
        if (jarEntry.isDirectory()) {
            treeNode.href = jarEntry.getName().substring(options.rootMarkdownFilePath.length()) + "/index.md";
            treeNode.level = Path.of(treeNode.href).getNameCount() - 1;
            treeNode.href = (options.rootURLPath + treeNode.href).replaceAll("/+", "/");

            List<JarEntry> jarEntries = KeelHelper.traversalInJar(jarEntry.getName());
            for (var childJarEntry : jarEntries) {
                var x = buildTreeNodeInJar(childJarEntry);
                if (x != null) treeNode.addChild(x);
            }
        } else {
            var fileName = Path.of(jarEntry.getName()).getFileName().toString();
            if (fileName.equalsIgnoreCase("index.md")) {
                return null;
            }
            if (!fileName.endsWith(".md")) {
                return null;
            }
            treeNode.href = jarEntry.getName().substring(options.rootMarkdownFilePath.length());
            treeNode.level = Path.of(treeNode.href).getNameCount();
            treeNode.href = (options.rootURLPath + treeNode.href).replaceAll("/+", "/");
        }
        return treeNode;
    }

    protected TreeNode buildTreeOutsideJAR() {
        File root = new File(actualFileRootOutsideJAR);
        options.logger.debug("buildTreeOutsideJAR " + root.getAbsolutePath());

        TreeNode tree = new TreeNode();
        tree.href = options.rootURLPath + "index.md";
        tree.name = options.subjectOfDocuments;
        tree.level = 0;

        if (root.isDirectory()) {
            options.logger.debug("IS DIR? " + root.isDirectory());
            File[] files = root.listFiles();
            if (files != null) {
                options.logger.debug("files total " + files.length);
                for (var file : files) {
                    var x = buildTreeNodeOutsideJar(file);
                    if (x != null) {
                        tree.addChild(x);
                    }
                }
            }
        }

        options.logger.debug("TREE", tree.toJsonObject());
        return tree;
    }

    private TreeNode buildTreeNodeOutsideJar(File item) {
        options.logger.debug("buildTreeNodeOutsideJar " + item.getAbsolutePath());
        String base = new File(actualFileRootOutsideJAR).getAbsolutePath();
        TreeNode treeNode = new TreeNode();
        if (item.isDirectory()) {
            treeNode.href = (item.getAbsolutePath().substring(base.length()) + "/index.md");
            treeNode.level = Path.of(treeNode.href).getNameCount() - 1;
            treeNode.href = (options.rootURLPath + treeNode.href).replaceAll("/+", "/");

            File[] files = item.listFiles();
            if (files != null) {
                for (var file : files) {
                    var x = buildTreeNodeOutsideJar(file);
                    if (x != null) treeNode.addChild(x);
                }
            }
        } else {
            if (!item.getName().endsWith(".md")) {
                return null;
            }
            if (item.getName().equalsIgnoreCase("index.md")) {
                return null;
            }
            treeNode.href = (options.rootURLPath + item.getAbsolutePath().substring(base.length()))
                    .replaceAll("/+", "/");
            treeNode.level = Path.of(treeNode.href).getNameCount();
        }
        treeNode.name = item.getName();
        return treeNode;
    }

    protected static class TreeNode {
        public String href;
        public String name;
        private final List<TreeNode> _children = new ArrayList<>();
        public int level;

        public void addChild(TreeNode child) {
            _children.add(child);
        }

        public JsonObject toJsonObject() {
            var x = new JsonObject()
                    .put("href", href)
                    .put("name", name)
                    .put("level", level);
            JsonArray array = new JsonArray();
            for (var child : _children) {
                array.add(child.toJsonObject());
            }
            x.put("children", array);
            return x;
        }

        public List<TreeNode> getSortedChildren() {
            _children.sort(Comparator.comparing(o -> o.name));
            return _children;
        }
    }
}
