package io.github.sinri.keel.logger.printer;

import io.vertx.core.Handler;
import io.vertx.core.VertxOptions;

import java.io.PrintStream;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 4.0.0
 */
public class KeelPrinter {

    private final PrintStream printStream;

    public KeelPrinter(PrintStream printStream) {
        this.printStream = printStream;
    }

    public KeelPrinter() {
        this(System.out);
    }

    public static void main(String[] args) {
        Keel.initializeVertxStandalone(new VertxOptions());

        KeelPrinter keelPrinter = new KeelPrinter();
        keelPrinter
                .print(sgr -> sgr
                        .reset()
                        .backgroundColor(SGRColor.YELLOW)
                        .foregroundColor(SGRColor.RED)
                )
                .print("===== WARNING =====")
                .print(ControlSequenceIntroducer.SelectGraphicRenditionBuilder::reset)
                .println()
                .print(sgr -> sgr
                        .reset()
                        .backgroundColor(SGRColor.YELLOW)
                        .foregroundColor(SGRColor.RED)
                )
                .print("|")
                .print(sgr -> sgr
                        .reset()
                        .backgroundColor(SGRColor.WHITE)
                        .foregroundColor(SGRColor.BLUE)
                )
                .print("Something Broken!")
                .print(ControlSequenceIntroducer.SelectGraphicRenditionBuilder::reset)
                .print(sgr -> sgr
                        .reset()
                        .backgroundColor(SGRColor.YELLOW)
                        .foregroundColor(SGRColor.RED)
                )
                .print("|")
                .print(ControlSequenceIntroducer.SelectGraphicRenditionBuilder::reset)
                .println()
                .print(sgr -> sgr
                        .reset()
                        .backgroundColor(SGRColor.YELLOW)
                        .foregroundColor(SGRColor.RED)
                )
                .print("===================")
                .print(ControlSequenceIntroducer.SelectGraphicRenditionBuilder::reset)
                .println();


        Keel.getVertx().setPeriodic(1000L, timer -> {
            keelPrinter.print(ControlSequenceIntroducer.eraseInLineEntire());
            keelPrinter.print(ControlSequenceIntroducer.moveCursorBack(11));

            int x = Keel.randomHelper().getRandom().nextInt() % 10;
            for (int i = 0; i < x; i++) {
                keelPrinter.print(sgr -> sgr
                                .reset()
                                .backgroundColor(SGRColor.RED)
                                .foregroundColor(SGRColor.CYAN)
                        )
                        .print("=")
                        .print(ControlSequenceIntroducer.SelectGraphicRenditionBuilder::reset);
            }
        });
    }

    public KeelPrinter println() {
        return println("");
    }

    public KeelPrinter print(String text) {
        printStream.print(text);
        return this;
    }

    public KeelPrinter println(String text) {
        printStream.println(text);
        return this;
    }

    public KeelPrinter print(ControlSequenceIntroducer csi) {
        printStream.print(csi.toString());
        return this;
    }

    public KeelPrinter print(Handler<ControlSequenceIntroducer.SelectGraphicRenditionBuilder> sgrBuilderHandler) {
        var built = ControlSequenceIntroducer.selectGraphicRendition(sgrBuilderHandler);
        return print(built);
    }

    /**
     * @param n 行
     * @param m 列
     */
    public record Location(int n, int m) {

    }
}
