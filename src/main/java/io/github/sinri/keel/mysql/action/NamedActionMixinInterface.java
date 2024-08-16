package io.github.sinri.keel.mysql.action;

import io.github.sinri.keel.core.SelfInterface;
import io.github.sinri.keel.mysql.NamedMySQLConnection;
import org.jetbrains.annotations.NotNull;


/**
 * @param <C>
 * @param <W>
 * @since 3.2.11
 */
public interface NamedActionMixinInterface<C extends NamedMySQLConnection, W>
        extends SelfInterface<W> {
    @NotNull
    C getNamedSqlConnection();
}
