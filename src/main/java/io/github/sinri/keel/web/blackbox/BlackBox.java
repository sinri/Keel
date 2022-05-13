package io.github.sinri.keel.web.blackbox;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.web.blackbox.html.HtmlElement;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.io.File;
import java.util.Date;

/**
 * A simple online log viewer
 *
 * @since 2.2
 */
public class BlackBox {
    private final String routeRootPath;
    private final String logDirPath;

    /**
     * @param routeRootPath start with '/', such as '/blackbox'
     * @param logDirPath    path such as '/var/log/xxx'
     */
    public BlackBox(String routeRootPath, String logDirPath) {
        this.routeRootPath = routeRootPath;
        this.logDirPath = logDirPath;
    }

    /**
     * Register it to Router
     *
     * @param router main router
     */
    public void registerToRoute(Router router) {
        KeelLogger logger = Keel.outputLogger("blackbox");
        router.get(this.routeRootPath + "/*")
                .failureHandler(routingContext -> {
                    logger.error("failure handler");
                    routingContext.response().setStatusCode(501).end();
                })
                .handler(routingContext -> {
                    String path = routingContext.request().path();
                    logger.info("incoming request path " + path);
                    if (path.startsWith(this.routeRootPath + "/see/")) {
                        String requestedPath = path.substring((this.routeRootPath + "/see/").length() - 1);
                        String targetPath = this.logDirPath + requestedPath;
                        File file = new File(targetPath);
                        if (!file.exists()) {
                            routingContext.response().setStatusCode(404).end();
                            return;
                        }
                        if (!file.canRead()) {
                            routingContext.response().setStatusCode(403).end();
                            return;
                        }
                        if (file.isDirectory()) {
                            handleDir(routingContext, file);
                        } else {
                            handleFile(routingContext, file);
                        }
                    } else {
                        routingContext.response().setStatusCode(500).end();
                    }
                });
    }

    private void handleDir(RoutingContext routingContext, File dir) {
        Keel.getVertx().fileSystem().readDir(dir.getAbsolutePath())
                .compose(children -> {
                    String relativeDirPath = dir.getAbsolutePath().substring(this.logDirPath.length());

                    boolean isRootDir = relativeDirPath.isEmpty();

                    String pageTitle = "BlackBox - " + relativeDirPath;

                    HtmlElement bodyElement = new HtmlElement("body")
                            .setAttributes("style='text-align: center;'");
                    bodyElement.addSubElement(
                            new HtmlElement("h1").setContent(pageTitle)
                    );
                    if (!isRootDir) {
                        File parent = dir.getParentFile();
                        String relativeParentDirPath = parent.getAbsolutePath().substring(this.logDirPath.length());

                        bodyElement.addSubElement(
                                new HtmlElement("div")
                                        .setAttributes("style='margin: 10px auto;'")
                                        .addSubElement(
                                                new HtmlElement("a")
                                                        .setAttributes("href='" + this.routeRootPath + "/see" + (relativeParentDirPath.isEmpty() ? "/" : relativeParentDirPath) + "'")
                                                        .setContent("Go to parent directory: " + parent.getName())
                                        )
                        );
                    }
                    bodyElement.addSubElement(
                            new HtmlElement("div").setContent("Totally " + children.size() + " child(ren).")
                    );
                    if (!children.isEmpty()) {
                        HtmlElement table = new HtmlElement("table")
                                .setAttributes("style='min-width:50%;margin: auto;'");

                        table.addSubElement(
                                new HtmlElement("tr")
                                        .addSubElement(new HtmlElement("th")
                                                .setAttributes("style='margin:5px 10px;border-bottom: gray 1px dashed;'")
                                                .setContent("Name")
                                        )
                                        .addSubElement(new HtmlElement("th")
                                                .setAttributes("style='margin:5px 10px;border-bottom: gray 1px dashed;'")
                                                .setContent("Last Modified")
                                        )
                        );

                        children.forEach(child -> {
                            File childFile = new File(child);
                            String relativeChildPath = child.substring(this.logDirPath.length());

                            table.addSubElement(
                                    new HtmlElement("tr")
                                            .addSubElement(new HtmlElement("td")
                                                    .setAttributes("style='margin:5px 10px;border-bottom: gray 1px dashed;'")
                                                    .addSubElement(new HtmlElement("a")
                                                            .setContent(childFile.getName())
                                                            .setAttributes("href='" + this.routeRootPath + "/see" + relativeChildPath + "'" + (childFile.isFile() ? " target='_blank'" : ""))
                                                    )
                                            )
                                            .addSubElement(new HtmlElement("td")
                                                    .setAttributes("style='margin:5px 10px;border-bottom: gray 1px dashed;'")
                                                    .setContent((new Date(childFile.lastModified()).toString()))
                                            )
                            );
                        });

                        bodyElement.addSubElement(table);
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("<!doctype html>\n");

                    var html = new HtmlElement("html")
                            .addSubElement(new HtmlElement("head")
                                    .addSubElement(new HtmlElement("title").setContent(pageTitle))
                            )
                            .addSubElement(bodyElement);
                    sb.append(html.toString());
                    return Future.succeededFuture(sb.toString());
                })
                .compose(code -> {
                    return routingContext.response().end(code);
                });
    }

    private void handleFile(RoutingContext routingContext, File file) {
        routingContext.response()
                .setChunked(true)
                .putHeader("Content-Type", "text/plain;charset=UTF-8")
                .sendFile(file.getAbsolutePath())
                .compose(v -> {
                    return routingContext.response().end();
                });

    }

}
