package io.github.sinri.keel.helper;

/**
 * @since 3.0.0
 * 使用此类可以实现无需启动 VERTX 即可使用 HELPER。
 */
public class KeelHelpers implements TraitForHelpers {
    private static final KeelHelpers instance = new KeelHelpers();

    private KeelHelpers() {
    }

    public static KeelHelpers getInstance() {
        return instance;
    }
}
