package io.github.sinri.keel.mysql.dev;

import io.github.sinri.keel.core.TechnicalPreview;
import org.jetbrains.annotations.NotNull;


/**
 * @since 3.1.0 Technical Preview
 * Use with @see AESValueEnvelope, bind a field to a certain AES Value Envelope.
 */
@TechnicalPreview(since = "3.1.0")
public class TableRowClassFieldAesEncryption {
    private final String envelopePackage;
    private final String envelopeName;

    public TableRowClassFieldAesEncryption(@NotNull String envelopeName, @NotNull String envelopePackage) {
        this.envelopePackage = envelopePackage;
        this.envelopeName = envelopeName;
    }

    public String buildCallClassMethodCode(@NotNull String parameter) {
        return "new " + envelopePackage + "." + envelopeName + "().decrypt(" + parameter + ");";
    }
}
