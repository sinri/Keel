package io.github.sinri.keel.test.lab.pdfbox;

import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.github.sinri.keel.tesuto.TestUnitResult;
import io.vertx.core.Future;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public class PDFBoxTest1 extends KeelTest {
    @Nonnull
    @Override
    protected Future<Void> starting() {
        return Future.succeededFuture();
    }

    @Nonnull
    @Override
    protected Future<Void> ending(List<TestUnitResult> testUnitResults) {
        return Future.succeededFuture();
    }

    // NOTE
    // -Dorg.apache.pdfbox.rendering.UsePureJavaCMYKConversion=true
    // which may improve the performance of rendering PDFs on some systems especially if there are a lot of images on a page.

    @TestUnit
    public Future<Void> test1() {
        try (PDDocument pdf = new PDDocument()) {
            PDPage page1 = new PDPage();
            pdf.addPage(page1);

            try (PDPageContentStream cont = new PDPageContentStream(pdf, page1)) {

                cont.beginText();

                cont.setFont(PDType1Font.TIMES_ROMAN, 12);
                cont.setLeading(14.5f);

                cont.newLineAtOffset(25, 700);
                String line1 = "World War II (often abbreviated to WWII or WW2), "
                        + "also known as the Second World War,";
                cont.showText(line1);

                cont.newLine();

                String line2 = "was a global war that lasted from 1939 to 1945, "
                        + "although related conflicts began earlier.";
                cont.showText(line2);
                cont.newLine();

                String line3 = "It involved the vast majority of the world's "
                        + "countries—including all of the great powers—";
                cont.showText(line3);
                cont.newLine();

                String line4 = "eventually forming two opposing military "
                        + "alliances: the Allies and the Axis.";
                cont.showText(line4);
                cont.newLine();

                cont.endText();
            }

            pdf.save("/Users/leqee/code/Keel/src/test/resources/pdf/wwii.pdf");
            return Future.succeededFuture();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
