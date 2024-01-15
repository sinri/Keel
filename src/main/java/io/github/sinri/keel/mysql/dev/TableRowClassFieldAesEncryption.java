package io.github.sinri.keel.mysql.dev;

import io.github.sinri.keel.core.TechnicalPreview;

import javax.annotation.Nonnull;

/**
 * @since 3.1.0 Technical Preview
 * Use with @see AESValueEnvelope, bind a field to a certain AES Value Envelope.
 */
@TechnicalPreview(since = "3.1.0")
public class TableRowClassFieldAesEncryption {
    private final String envelopePackage;
    private final String envelopeName;

    public TableRowClassFieldAesEncryption(@Nonnull String envelopeName, @Nonnull String envelopePackage) {
        this.envelopePackage = envelopePackage;
        this.envelopeName = envelopeName;
    }

    public String buildCallClassMethodCode(@Nonnull String parameter) {
        return "new " + envelopePackage + "." + envelopeName + "().decrypt(" + parameter + ");";
    }
}
