package io.github.sinri.keel.logger.event.logger;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;

abstract public class KeelFreeEventLogger implements KeelEventLogger {
    @Override
    final public Keel getKeel() {
        return null;
    }
}
