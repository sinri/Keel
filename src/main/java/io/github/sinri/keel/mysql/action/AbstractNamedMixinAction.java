package io.github.sinri.keel.mysql.action;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import org.jetbrains.annotations.NotNull;


/**
 * @param <C>
 * @since 3.2.11 Refined for Mixin Style, extracted NamedActionInterface.
 */
public abstract class AbstractNamedMixinAction<C extends NamedMySQLConnection, W> implements NamedActionMixinInterface<C, W> {
    private final @NotNull C namedSqlConnection;

    public AbstractNamedMixinAction(@NotNull C namedSqlConnection) {
        this.namedSqlConnection = namedSqlConnection;
    }

    @NotNull
    @Override
    public final C getNamedSqlConnection() {
        return namedSqlConnection;
    }
}
