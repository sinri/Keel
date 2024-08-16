package io.github.sinri.keel.mysql.statement.templated;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;

/**
 * @since 3.0.8
 */
public class TemplateArgumentMapping extends HashMap<String, TemplateArgument> {
    public TemplateArgumentMapping bindNull(@NotNull String argumentName) {
        this.put(argumentName, TemplateArgument.forNull());
        return this;
    }

    public TemplateArgumentMapping bindNumber(@NotNull String argumentName, @NotNull Number number) {
        this.put(argumentName, TemplateArgument.forNumber(number));
        return this;
    }

    /**
     * @param numbers      Ensure not empty or use with bindLineCommentStarting
     */
    public TemplateArgumentMapping bindNumbers(@NotNull String argumentName, @NotNull Collection<? extends Number> numbers) {
        this.put(argumentName, TemplateArgument.forNumbers(numbers));
        return this;
    }

    public TemplateArgumentMapping bindString(@NotNull String argumentName, @NotNull String string) {
        this.put(argumentName, TemplateArgument.forString(string));
        return this;
    }

    /**
     * @param strings      Ensure not empty or use with bindLineCommentStarting
     */
    public TemplateArgumentMapping bindStrings(@NotNull String argumentName, @NotNull Collection<String> strings) {
        this.put(argumentName, TemplateArgument.forStrings(strings));
        return this;
    }

    public TemplateArgumentMapping bindExpression(@NotNull String argumentName, @NotNull String expression) {
        this.put(argumentName, TemplateArgument.forExpression(expression));
        return this;
    }

    /**
     * @param expressions  Ensure not empty or use with bindLineCommentStarting
     */
    public TemplateArgumentMapping bindExpressions(@NotNull String argumentName, @NotNull Collection<String> expressions) {
        this.put(argumentName, TemplateArgument.forExpressions(expressions));
        return this;
    }

    /**
     * @since 3.0.11
     */
    public TemplateArgumentMapping bindLineCommentStarting(@NotNull String argumentName, boolean commentFromHere) {
        this.put(argumentName, TemplateArgument.forExpression((commentFromHere ? "-- " : " ")));
        return this;
    }
}
