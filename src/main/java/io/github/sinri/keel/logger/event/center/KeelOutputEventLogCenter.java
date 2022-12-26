package io.github.sinri.keel.logger.event.center;

import io.github.sinri.keel.logger.event.adapter.OutputAdapter;

public class KeelOutputEventLogCenter extends KeelSyncEventLogCenter {
    private final static KeelOutputEventLogCenter instance = new KeelOutputEventLogCenter();

    public KeelOutputEventLogCenter() {
        super(OutputAdapter.getInstance());
    }

    public static KeelOutputEventLogCenter getInstance() {
        return instance;
    }
}
