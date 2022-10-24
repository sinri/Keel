package io.github.sinri.keel.core.helper;

public class KeelHelpers {
    private static final KeelHelpers instance;

    static {
        instance = new KeelHelpers();
    }

    private final KeelBinaryHelper binaryHelper;
    private final KeelDateTimeHelper dateTimeHelper;
    private final KeelFileHelper fileHelper;
    private final KeelJsonHelper jsonHelper;
    private final KeelNetHelper netHelper;
    private final KeelReflectionHelper reflectionHelper;
    private final KeelStringHelper stringHelper;
    private final KeelCryptographyHelper cryptographyHelper;
    private final KeelDigestHelper digestHelper;

    private KeelHelpers() {
        this.binaryHelper = KeelBinaryHelper.getInstance();
        this.dateTimeHelper = KeelDateTimeHelper.getInstance();
        this.fileHelper = KeelFileHelper.getInstance();
        this.jsonHelper = KeelJsonHelper.getInstance();
        this.netHelper = KeelNetHelper.getInstance();
        this.reflectionHelper = KeelReflectionHelper.getInstance();
        this.stringHelper = KeelStringHelper.getInstance();
        this.cryptographyHelper = KeelCryptographyHelper.getInstance();
        this.digestHelper = KeelDigestHelper.getInstance();
    }

    public static KeelHelpers getInstance() {
        return instance;
    }

    public KeelBinaryHelper binary() {
        return binaryHelper;
    }

    public KeelDateTimeHelper datetime() {
        return dateTimeHelper;
    }

    public KeelFileHelper file() {
        return fileHelper;
    }

    public KeelJsonHelper json() {
        return jsonHelper;
    }

    public KeelNetHelper net() {
        return netHelper;
    }

    public KeelReflectionHelper reflection() {
        return reflectionHelper;
    }

    public KeelStringHelper string() {
        return stringHelper;
    }

    public KeelCryptographyHelper cryptography() {
        return cryptographyHelper;
    }

    public KeelDigestHelper digest() {
        return digestHelper;
    }
}
