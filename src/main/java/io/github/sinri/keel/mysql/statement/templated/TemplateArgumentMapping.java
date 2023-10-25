package io.github.sinri.keel.mysql.statement.templated;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;

/**
 * @since 3.0.8
 */
public class TemplateArgumentMapping extends HashMap<String, TemplateArgument> {
    public TemplateArgumentMapping bindNull(@Nonnull String argumentName) {
        this.put(argumentName, TemplateArgument.forNull());
        return this;
    }

    public TemplateArgumentMapping bindNumber(@Nonnull String argumentName, @Nonnull Number number) {
        this.put(argumentName, TemplateArgument.forNumber(number));
        return this;
    }

    public TemplateArgumentMapping bindNumbers(@Nonnull String argumentName, @Nonnull Collection<Number> numbers) {
        this.put(argumentName, TemplateArgument.forNumbers(numbers));
        return this;
    }

    public TemplateArgumentMapping bindString(@Nonnull String argumentName, @Nonnull String string) {
        this.put(argumentName, TemplateArgument.forString(string));
        return this;
    }

    public TemplateArgumentMapping bindStrings(@Nonnull String argumentName, @Nonnull Collection<String> strings) {
        this.put(argumentName, TemplateArgument.forStrings(strings));
        return this;
    }

    public TemplateArgumentMapping bindExpression(@Nonnull String argumentName, @Nonnull String expression) {
        this.put(argumentName, TemplateArgument.forExpression(expression));
        return this;
    }

    public TemplateArgumentMapping bindExpressions(@Nonnull String argumentName, @Nonnull Collection<String> expressions) {
        this.put(argumentName, TemplateArgument.forExpressions(expressions));
        return this;
    }
}
