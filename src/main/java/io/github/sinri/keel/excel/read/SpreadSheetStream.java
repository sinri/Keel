package io.github.sinri.keel.excel.read;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.servant.intravenous.KeelIntravenous;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@TechnicalPreview(since = "3.0.12")
public class SpreadSheetStream {
    private final List<List<String>> headers;
    private final KeelIntravenous<List<String>> intravenous;
    private final AtomicReference<String> deploymentIdRef = new AtomicReference<>();

    public SpreadSheetStream(Function<List<List<String>>, Future<Void>> processor, int batchSize) {
        headers = new ArrayList<>();
        intravenous = new KeelIntravenous<>(processor)
                .setBatchSize(batchSize)
                .setSleepTime(10L);
    }

    public Future<Void> startIntravenous() {
        return intravenous.deployMe(new DeploymentOptions()
                        .setInstances(1)
                        .setWorker(true)
                )
                .compose(deploymentId -> {
                    deploymentIdRef.set(deploymentId);
                    return Future.succeededFuture();
                });
    }

    public List<List<String>> getHeaders() {
        return headers;
    }

    public SpreadSheetStream addHeaderRow(List<String> headerRow) {
        this.headers.add(headerRow);
        return this;
    }


    public void handleRow(List<String> rawRow) {
        this.intravenous.add(rawRow);
    }

    @Deprecated
    public Future<Void> allRowsAnalyzed() {
        return this.intravenous.shutdown();
    }

    public Future<Void> shutdownIntravenous() {
        return this.intravenous.shutdown();
    }

    @Deprecated
    public boolean isRunning() {
        String deploymentId = deploymentIdRef.get();
        if (deploymentId == null) return false;
        return Keel.getVertx().deploymentIDs().contains(deploymentId);
    }
}
