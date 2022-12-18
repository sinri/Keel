package io.github.sinri.keel.helper;

/**
 * @since 3.0.0
 */
public interface TraitForHelpers {
    default KeelBinaryHelper binaryHelper() {
        return KeelBinaryHelper.getInstance();
    }

    default KeelDateTimeHelper datetimeHelper() {
        return KeelDateTimeHelper.getInstance();
    }

    default KeelFileHelper fileHelper() {
        return KeelFileHelper.getInstance();
    }

    default KeelJsonHelper jsonHelper() {
        return KeelJsonHelper.getInstance();
    }

    default KeelNetHelper netHelper() {
        return KeelNetHelper.getInstance();
    }

    default KeelReflectionHelper reflectionHelper() {
        return KeelReflectionHelper.getInstance();
    }

    default KeelStringHelper stringHelper() {
        return KeelStringHelper.getInstance();
    }

    default KeelCryptographyHelper cryptographyHelper() {
        return KeelCryptographyHelper.getInstance();
    }

    default KeelDigestHelper digestHelper() {
        return KeelDigestHelper.getInstance();
    }

    /**
     * @since 2.9.3
     */
    default KeelRuntimeHelper runtimeHelper() {
        return KeelRuntimeHelper.getInstance();
    }

    /**
     * @since 2.9.4
     */
    default KeelAuthenticationHelper authenticationHelper() {
        return KeelAuthenticationHelper.getInstance();
    }
}
