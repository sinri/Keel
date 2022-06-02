package io.github.sinri.keel.core.logger2;

/**
 * Print contents to STDOUT.
 */
public class KeelPrintLogger extends AbstractKeelLogger {

    public KeelPrintLogger(KeelLoggerOptions options) {
        super(options);
    }

    @Override
    public void text(String text) {
        System.out.print(text);
    }

    @Override
    public void text(String text, String lineEnding) {
        if (lineEnding == null) {
            System.out.println(text);
        } else {
            System.out.print(text + lineEnding);
        }
    }

    @Override
    public void text(KeelLogLevel logLevel, String text, String lineEnding) {
        if (this.isThisLevelVisible(logLevel)) {
            text(text, lineEnding);
        }
    }
}
