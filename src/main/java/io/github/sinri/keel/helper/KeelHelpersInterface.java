package io.github.sinri.keel.helper;

import io.github.sinri.keel.core.TechnicalPreview;

/**
 * @since 3.1.0
 */
@TechnicalPreview(since = "3.1.0")
public interface KeelHelpersInterface {
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


    default KeelRuntimeHelper runtimeHelper() {
        return KeelRuntimeHelper.getInstance();
    }

    default KeelAuthenticationHelper authenticationHelper() {
        return KeelAuthenticationHelper.getInstance();
    }

    default KeelRandomHelper randomHelper() {
        return KeelRandomHelper.getInstance();
    }
}
