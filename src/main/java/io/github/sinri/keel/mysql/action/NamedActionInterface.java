package io.github.sinri.keel.mysql.action;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import org.jetbrains.annotations.NotNull;



/**
 * @param <C>
 * @since 3.2.11
 */
public interface NamedActionInterface<C extends NamedMySQLConnection> {
    @NotNull
    C getNamedSqlConnection();
}
