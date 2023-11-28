package io.github.sinri.keel.mysql.statement.templated;


import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.mysql.Quoter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @since 3.0.8
 */
public class TemplateArgument {
    private final boolean asScalar;
    private final Collection<String> expressions;

    private TemplateArgument(String expression) {
        this.asScalar = true;
        this.expressions = List.of(expression);
    }

    private TemplateArgument(Collection<String> expressions) {
        this.asScalar = false;
        this.expressions = expressions;
    }

    public static TemplateArgument forNull() {
        return forExpression("NULL");
    }

    public static TemplateArgument forNumber(@Nonnull Number number) {
        return forExpression(String.valueOf(number));
    }

    public static TemplateArgument forNumbers(@Nonnull Collection<? extends Number> numbers) {
        List<String> list = new ArrayList<>();
        numbers.forEach(number -> {
            list.add(String.valueOf(number));
        });
        return forExpressions(list);
    }

    public static TemplateArgument forString(@Nonnull String string) {
        String s1 = Quoter.escapeString(string);
//        System.out.println("S1 | "+s1);
        String s2 = Quoter.quoteEscapedString(s1);
//        System.out.println("S2 | "+s2);
        return forExpression(s2);
    }

    public static TemplateArgument forStrings(@Nonnull Collection<String> strings) {
        List<String> list = new ArrayList<>();
        strings.forEach(string -> {
            list.add(Quoter.quoteEscapedString(Quoter.escapeString(string)));
        });
        return forExpressions(list);
    }

    public static TemplateArgument forExpression(@Nonnull String string) {
        return new TemplateArgument(string);
    }

    /**
     * @param strings
     * @return
     * @since 3.0.11 the provided collection could be empty, leave the error to Database.
     */
    public static TemplateArgument forExpressions(@Nonnull Collection<String> strings) {
        // if (strings.isEmpty()) throw new IllegalArgumentException();
        return new TemplateArgument(strings);
    }

    @Override
    public String toString() {
        if (this.asScalar) {
            for (String e : this.expressions) {
                return e;
            }
            throw new RuntimeException();
        } else {
            return "(" + KeelHelpers.stringHelper().joinStringArray(expressions, ",") + ")";
        }
    }
}
