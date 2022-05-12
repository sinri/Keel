package io.github.sinri.keel.web.blackbox.html;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @since 2.2
 */
public class HtmlElement {
    private final String tag;
    private final boolean paired;
    private final List<HtmlElement> subElementList = new ArrayList<>();
    private String attributes;
    private String content;

    public HtmlElement(String tag) {
        this.tag = tag;
        this.paired = !tag.equalsIgnoreCase("br");
        this.attributes = null;
        this.content = null;
    }

    public HtmlElement(String tag, boolean paired) {
        this.tag = tag;
        this.paired = paired;
        this.attributes = null;
        this.content = null;
    }

    public HtmlElement setAttributes(String attributes) {
        this.attributes = attributes;
        return this;
    }

    public HtmlElement setContent(String content) {
        this.content = content;
        return this;
    }

    public HtmlElement addSubElement(HtmlElement subHtmlElement) {
        this.subElementList.add(subHtmlElement);
        return this;
    }

    public String toString() {
        String a = "";
        if (attributes != null) {
            a = attributes;
            if (!a.isEmpty()) {
                a = " " + a;
            }
        }
        AtomicReference<String> c = new AtomicReference<>("");
        if (content != null) {
            c.set(content);
        } else {
            this.subElementList.forEach(subElement -> {
                c.getAndUpdate(ori -> {
                    return ori + subElement.toString();
                });
            });
        }
        String b = "<" + tag + a + ">";
        String e = "";
        if (paired) {
            e = "</" + tag + ">";
        }
        return b + c.get() + e;
    }
}
