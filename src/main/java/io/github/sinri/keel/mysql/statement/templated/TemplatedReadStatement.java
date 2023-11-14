package io.github.sinri.keel.mysql.statement.templated;

import io.github.sinri.keel.mysql.statement.AbstractReadStatement;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;

/**
 * @since 3.0.8
 */
public class TemplatedReadStatement extends AbstractReadStatement implements TemplatedStatement {

    private final String templateSql;
    private final TemplateArgumentMapping argumentMapping;

    public TemplatedReadStatement(@Nonnull String templateSql) {
        this.templateSql = templateSql;
        this.argumentMapping = new TemplateArgumentMapping();
    }

    public TemplatedReadStatement bindArguments(Handler<TemplateArgumentMapping> binder) {
        binder.handle(this.argumentMapping);
        return this;
    }


    @Override
    public String toString() {
        return this.build();
    }

    @Override
    public String getSqlTemplate() {
        return templateSql;
    }

    @Override
    public TemplateArgumentMapping getArguments() {
        return argumentMapping;
    }

}
