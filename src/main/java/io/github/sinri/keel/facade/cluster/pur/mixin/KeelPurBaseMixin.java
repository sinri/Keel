package io.github.sinri.keel.facade.cluster.pur.mixin;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.cluster.pur.KeelPurNodeInfo;
import io.github.sinri.keel.facade.cluster.pur.KeelPurNodeManager;
import io.github.sinri.keel.facade.cluster.pur.config.KeelPurConfig;
import io.github.sinri.keel.verticles.KeelVerticle;

/**
 * @since 3.1.3
 */
@TechnicalPreview(since = "3.1.3")
public interface KeelPurBaseMixin extends KeelVerticle {
    KeelPurConfig getConfig();

    KeelPurNodeInfo getLocalNodeInfo();

    KeelPurNodeManager getNodeManager();
}
