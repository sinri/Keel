package io.github.sinri.keel.helper;

/**
 * @since 3.0.0
 * 使用此类可以实现无需启动 VERTX 即可使用 HELPER。
 */
public interface KeelHelpers {

    static KeelBinaryHelper binaryHelper() {
        return KeelBinaryHelper.getInstance();
    }

    static KeelDateTimeHelper datetimeHelper() {
        return KeelDateTimeHelper.getInstance();
    }

    static KeelFileHelper fileHelper() {
        return KeelFileHelper.getInstance();
    }

    static KeelJsonHelper jsonHelper() {
        return KeelJsonHelper.getInstance();
    }

    static KeelNetHelper netHelper() {
        return KeelNetHelper.getInstance();
    }

    static KeelReflectionHelper reflectionHelper() {
        return KeelReflectionHelper.getInstance();
    }

    static KeelStringHelper stringHelper() {
        return KeelStringHelper.getInstance();
    }

    static KeelCryptographyHelper cryptographyHelper() {
        return KeelCryptographyHelper.getInstance();
    }

    static KeelDigestHelper digestHelper() {
        return KeelDigestHelper.getInstance();
    }

    /**
     * @since 2.9.3
     */
    static KeelRuntimeHelper runtimeHelper() {
        return KeelRuntimeHelper.getInstance();
    }

    /**
     * @since 2.9.4
     */
    static KeelAuthenticationHelper authenticationHelper() {
        return KeelAuthenticationHelper.getInstance();
    }

    static KeelRandomHelper randomHelper() {
        return KeelRandomHelper.getInstance();
    }
}
