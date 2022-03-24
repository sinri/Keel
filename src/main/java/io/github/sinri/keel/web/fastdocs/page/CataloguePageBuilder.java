package io.github.sinri.keel.web.fastdocs.page;

import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;

public class CataloguePageBuilder implements FastDocsContentResponder {
    private final PageBuilderOptions options;
    private final boolean embedded;
    private final String actualFileRootOutsideJAR;

    public CataloguePageBuilder(PageBuilderOptions options) {
        this.options = options;

        URL x = KeelPropertiesReader.class.getClassLoader().getResource(this.options.rootMarkdownFilePath);
        if (x == null) {
            throw new IllegalArgumentException("rootMarkdownFilePath is not available in File System");
        }
        this.embedded = x.toString().contains("!/");
        this.actualFileRootOutsideJAR = x.getPath();
        options.logger.info("EMBEDDED: " + embedded + " url: " + x + " actualFileRootOutsideJAR: " + actualFileRootOutsideJAR);
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
        if (embedded) {
            return createHTMLCodeForDir(buildTreeInsideJAR()).toString();
        } else {
            return createHTMLCodeForDir(buildTreeOutsideJAR()).toString();
        }
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

        sb.append("<div>");
        sb.append("<div style='display: inline-block;width:20px;border-left: 1px solid lightgrey;'>&nbsp;</div>".repeat(Math.max(0, tree.level)));
        sb.append("<div class='dir_box_title' style='display: inline-block;'>")
                .append("<span>\uD83D\uDCC1&nbsp;</span>")
                .append("<span>")
                .append("<a href='").append(boxHref).append("' ").append(isFromDoc() ? "target='_parent'" : "").append(" >").append(displayDirName).append("</a>")
                .append("</span>")
                .append("</div>");
        sb.append("</div>");

        // DIRS start
        if (tree.href.endsWith("/index.md")) {
            // as dir
            for (var child : tree.children) {
                if (child.href.endsWith("/index.md")) {
                    sb.append(createHTMLCodeForDir(child));
                } else {
                    sb.append("<div class='dir_box_body_item'>")
                            .append("<div style='display: inline-block;width:20px;border-left: 1px solid lightgrey;'>&nbsp;</div>".repeat(Math.max(0, child.level + 1)))
                            .append("<span>\uD83D\uDCC4&nbsp;</span>")
                            .append("<span>")
                            .append("<a href='")
                            .append(child.href)
                            .append("' ")
                            .append(isFromDoc() ? "target='_parent'" : "")
                            .append(" >")
                            .append(child.name)
                            .append("</a>")
                            .append("</span>")
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
                tree.children.add(child);
            }
        }
        options.logger.info("TREE", tree.toJsonObject());
        return tree;
    }

    private TreeNode buildTreeNodeInJar(JarEntry jarEntry) {
        options.logger.info("buildTreeNodeInJar: " + jarEntry.getName() + " isDir: " + jarEntry.isDirectory());
        TreeNode treeNode = new TreeNode();
        treeNode.name = String.valueOf(Path.of(jarEntry.getName()).getFileName());
        if (jarEntry.isDirectory()) {
            treeNode.href = jarEntry.getName().substring(options.rootMarkdownFilePath.length()) + "/index.md";
            treeNode.level = Path.of(treeNode.href).getNameCount() - 1;
            treeNode.href = (options.rootURLPath + treeNode.href).replaceAll("/+", "/");

            List<JarEntry> jarEntries = KeelHelper.traversalInJar(jarEntry.getName());
            for (var childJarEntry : jarEntries) {
                var x = buildTreeNodeInJar(childJarEntry);
                if (x != null) treeNode.children.add(x);
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
        options.logger.info("buildTreeOutsideJAR " + root.getAbsolutePath());

        TreeNode tree = new TreeNode();
        tree.href = options.rootURLPath + "index.md";
        tree.name = options.subjectOfDocuments;
        tree.level = 0;

        if (root.isDirectory()) {
            options.logger.info("IS DIR? " + root.isDirectory());
            File[] files = root.listFiles();
            if (files != null) {
                options.logger.info("files total " + files.length);
                for (var file : files) {
                    var x = buildTreeNodeOutsideJar(file);
                    if (x != null) {
                        tree.children.add(x);
                    }
                }
            }
        }

        options.logger.info("TREE", tree.toJsonObject());
        return tree;
    }

    private TreeNode buildTreeNodeOutsideJar(File item) {
        options.logger.info("buildTreeNodeOutsideJar " + item.getAbsolutePath());
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
                    if (x != null) treeNode.children.add(x);
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
        public List<TreeNode> children = new ArrayList<>();
        public int level;

        public JsonObject toJsonObject() {
            var x = new JsonObject()
                    .put("href", href)
                    .put("name", name)
                    .put("level", level);
            JsonArray array = new JsonArray();
            for (var child : children) {
                array.add(child.toJsonObject());
            }
            x.put("children", array);
            return x;
        }
    }
}
