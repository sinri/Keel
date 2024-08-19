package io.github.sinri.keel.logger.printer;

/**
 * @since 4.0.0
 */
public enum SGRColor {
    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7);

    private final int codeDelta;

    SGRColor(int codeDelta) {
        this.codeDelta = codeDelta;
    }

    public byte getCodeAsForegroundColor() {
        return (byte) (codeDelta + 30);
    }

    public byte getCodeAsBackgroundColor() {
        return (byte) (codeDelta + 40);
    }
}
