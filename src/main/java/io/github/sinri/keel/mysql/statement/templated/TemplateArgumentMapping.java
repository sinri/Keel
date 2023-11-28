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

    /**
     * @param argumentName
     * @param numbers      Ensure not empty or use with bindLineCommentStarting
     * @return
     */
    public TemplateArgumentMapping bindNumbers(@Nonnull String argumentName, @Nonnull Collection<? extends Number> numbers) {
        this.put(argumentName, TemplateArgument.forNumbers(numbers));
        return this;
    }

    public TemplateArgumentMapping bindString(@Nonnull String argumentName, @Nonnull String string) {
        this.put(argumentName, TemplateArgument.forString(string));
        return this;
    }

    /**
     * @param argumentName
     * @param strings      Ensure not empty or use with bindLineCommentStarting
     * @return
     */
    public TemplateArgumentMapping bindStrings(@Nonnull String argumentName, @Nonnull Collection<String> strings) {
        this.put(argumentName, TemplateArgument.forStrings(strings));
        return this;
    }

    public TemplateArgumentMapping bindExpression(@Nonnull String argumentName, @Nonnull String expression) {
        this.put(argumentName, TemplateArgument.forExpression(expression));
        return this;
    }

    /**
     * @param argumentName
     * @param expressions  Ensure not empty or use with bindLineCommentStarting
     * @return
     */
    public TemplateArgumentMapping bindExpressions(@Nonnull String argumentName, @Nonnull Collection<String> expressions) {
        this.put(argumentName, TemplateArgument.forExpressions(expressions));
        return this;
    }

    /**
     * @since 3.0.11
     */
    public TemplateArgumentMapping bindLineCommentStarting(@Nonnull String argumentName, boolean commentFromHere) {
        this.put(argumentName, TemplateArgument.forExpression((commentFromHere ? "-- " : " ")));
        return this;
    }
}
