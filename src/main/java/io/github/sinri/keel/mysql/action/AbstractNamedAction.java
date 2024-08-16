package io.github.sinri.keel.mysql.action;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import org.jetbrains.annotations.NotNull;


/**
 * @param <C>
 * @since 3.2.11 Moved from `io.github.sinri.keel.mysql.AbstractNamedAction` and Refined.
 */
public abstract class AbstractNamedAction<C extends NamedMySQLConnection> implements NamedActionInterface<C> {
    private final @NotNull C namedSqlConnection;

    public AbstractNamedAction(@NotNull C namedSqlConnection) {
        this.namedSqlConnection = namedSqlConnection;
    }

    @NotNull
    @Override
    public C getNamedSqlConnection() {
        return namedSqlConnection;
    }
}
