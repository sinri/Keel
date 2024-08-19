package io.github.sinri.keel.logger.printer;

import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://en.wikipedia.org/wiki/ANSI_escape_code">ANSI escape code</a>
 * @since 4.0.0
 */
public class ControlSequenceIntroducer {
    private final String csi;

    ControlSequenceIntroducer(String csi) {
        this.csi = csi;
    }

    public static ControlSequenceIntroducer factory(String command, int... codes) {
        StringBuilder sb = new StringBuilder("\033[");
        for (int i = 0; i < codes.length; i++) {
            if (i > 0) {
                sb.append(";");
            }
            sb.append(codes[i]);
        }
        sb.append(command);
        return new ControlSequenceIntroducer(sb.toString());
    }

    /**
     * `CSI n ; m H`
     * 光标移动到第n行、第m列。值从1开始，且默认为1（左上角）。
     * 例如CSI ;5H和CSI 1;5H含义相同；CSI 17;H、CSI 17H和CSI 17;1H三者含义相同。
     */
    public static ControlSequenceIntroducer moveCursorPosition(int n, int m) {
        return factory("H", n, m);
    }

    /**
     * `CSI n K`
     * 清除行内的部分区域。
     * 如果n是0（或缺失），清除从光标位置到该行末尾的部分。
     */
    public static ControlSequenceIntroducer eraseInLineToRight() {
        return factory("K", 0);
    }

    /**
     * `CSI n K`
     * 清除行内的部分区域。
     * 如果n是1，清除从光标位置到该行开头的部分。
     */
    public static ControlSequenceIntroducer eraseInLineToLeft() {
        return factory("K", 1);
    }

    /**
     * `CSI n K`
     * 清除行内的部分区域。
     * 如果n是2，清除整行。光标位置不变。
     */
    public static ControlSequenceIntroducer eraseInLineEntire() {
        return factory("K", 2);
    }

    public static ControlSequenceIntroducer moveCursorUp(int x) {
        return factory("A", x);
    }

    public static ControlSequenceIntroducer moveCursorDown(int x) {
        return factory("B", x);
    }

    public static ControlSequenceIntroducer moveCursorForward(int x) {
        return factory("C", x);
    }

    public static ControlSequenceIntroducer moveCursorBack(int x) {
        return factory("D", x);
    }

    public static ControlSequenceIntroducer selectGraphicRendition(Handler<SelectGraphicRenditionBuilder> handler) {
        SelectGraphicRenditionBuilder selectGraphicRenditionBuilder = new SelectGraphicRenditionBuilder();
        handler.handle(selectGraphicRenditionBuilder);
        return selectGraphicRenditionBuilder.build();
    }

//    /**
//     * CSI 6n
//     * DSR – 设备状态报告（Device Status Report）
//     * 以ESC[n;mR（就像在键盘上输入）向应用程序报告光标位置（CPR），其中n是行，m是列。
//     */
//    public static ControlSequenceIntroducer deviceStatusReport() {
//        return factory("n", 6);
//    }

    @Override
    public String toString() {
        return csi;
    }

    public static class SelectGraphicRenditionBuilder {
        List<Byte> codes = new ArrayList<>();

        public SelectGraphicRenditionBuilder() {

        }

        public SelectGraphicRenditionBuilder reset() {
            codes.add((byte) 0);
            return this;
        }

        public SelectGraphicRenditionBuilder foregroundColor(SGRColor color) {
            codes.add(color.getCodeAsForegroundColor());
            return this;
        }

        public SelectGraphicRenditionBuilder backgroundColor(SGRColor color) {
            codes.add(color.getCodeAsBackgroundColor());
            return this;
        }

        public ControlSequenceIntroducer build() {
            int[] array = new int[codes.size()];
            for (int i = 0; i < codes.size(); i++) {
                array[i] = codes.get(i);
            }
            return ControlSequenceIntroducer.factory("m", array);
        }
    }

}
