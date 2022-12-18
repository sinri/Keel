package io.github.sinri.keel.web.blackbox;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.web.blackbox.html.HTMLElement;
import io.github.sinri.keel.web.blackbox.html.HTMLTagElement;
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
    private final Keel keel;
    private final String routeRootPath;
    private final String logDirPath;
    private final KeelEventLogger logger;

    /**
     * @param routeRootPath start with '/', such as '/blackbox'
     * @param logDirPath    path such as '/var/log/xxx'
     */
    public BlackBox(Keel keel, String routeRootPath, String logDirPath) {
        this.keel = keel;
        this.logger = this.keel.createOutputEventLogger("blackbox");
        this.routeRootPath = routeRootPath;
        this.logDirPath = logDirPath;
    }

    /**
     * Register it to Router
     *
     * @param router main router
     */
    public void registerToRoute(Router router) {

        router.get(this.routeRootPath + "/*")
                .failureHandler(routingContext -> {
                    logger.error("failure handler");
                    routingContext.response().setStatusCode(501).end();
                })
                .blockingHandler(routingContext -> {
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
        keel.fileSystem().readDir(dir.getAbsolutePath())
                .compose(children -> {
                    String relativeDirPath = dir.getAbsolutePath().substring(this.logDirPath.length());

                    boolean isRootDir = relativeDirPath.isEmpty();

                    String pageTitle = "BlackBox - " + relativeDirPath;

                    HTMLTagElement bodyElement = new HTMLTagElement("body")
                            .setAttributes("style='text-align: center;'");
                    bodyElement.addSubElement(
                            new HTMLTagElement("h1").setContent(pageTitle)
                    );
                    if (!isRootDir) {
                        File parent = dir.getParentFile();
                        String relativeParentDirPath = parent.getAbsolutePath().substring(this.logDirPath.length());

                        bodyElement.addSubElement(
                                new HTMLTagElement("div")
                                        .setAttributes("style='margin: 10px auto;'")
                                        .addSubElement(
                                                new HTMLTagElement("a")
                                                        .setAttributes("href='" + this.routeRootPath + "/see" + (relativeParentDirPath.isEmpty() ? "/" : relativeParentDirPath) + "'")
                                                        .setContent("Go to parent directory: " + parent.getName())
                                        )
                        );
                    }
                    bodyElement.addSubElement(
                            new HTMLTagElement("div").setContent("Totally " + children.size() + " child(ren).")
                    );
                    if (!children.isEmpty()) {
                        HTMLTagElement table = new HTMLTagElement("table")
                                .setAttributes("style='min-width:50%;margin: auto;'");

                        table.addSubElement(
                                new HTMLTagElement("tr")
                                        .addSubElement(new HTMLTagElement("th")
                                                .setAttributes("style='margin:5px 10px;border-bottom: gray 1px dashed;'")
                                                .setContent("Name")
                                        )
                                        .addSubElement(new HTMLTagElement("th")
                                                .setAttributes("style='margin:5px 10px;border-bottom: gray 1px dashed;'")
                                                .setContent("Last Modified")
                                        )
                        );

                        children.forEach(child -> {
                            File childFile = new File(child);
                            String relativeChildPath = child.substring(this.logDirPath.length());

                            table.addSubElement(
                                    new HTMLTagElement("tr")
                                            .addSubElement(new HTMLTagElement("td")
                                                    .setAttributes("style='margin:5px 10px;border-bottom: gray 1px dashed;'")
                                                    .addSubElement(new HTMLTagElement("a")
                                                            .setContent(childFile.getName())
                                                            .setAttributes("href='" + this.routeRootPath + "/see" + relativeChildPath + "'" + (childFile.isFile() ? " target='_blank'" : ""))
                                                    )
                                            )
                                            .addSubElement(new HTMLTagElement("td")
                                                    .setAttributes("style='margin:5px 10px;border-bottom: gray 1px dashed;'")
                                                    .setContent((new Date(childFile.lastModified()).toString()))
                                            )
                            );
                        });

                        bodyElement.addSubElement(table);
                    }

                    return Future.succeededFuture(
                            new HTMLElement(
                                    new HTMLTagElement("html")
                                            .addSubElement(new HTMLTagElement("head")
                                                    .addSubElement(new HTMLTagElement("title").setContent(pageTitle))
                                            )
                                            .addSubElement(bodyElement)
                            )
                    );
                })
                .compose(html -> {
                    return routingContext.response().end(html.toString());
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
