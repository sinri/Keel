package io.github.sinri.keel.mysql;

/**
 * @param <C>
 * @since 3.0.11 Technical Preview
 * @since 3.0.18 Finished Technical Preview.
 */
public class AbstractNamedAction<C extends NamedMySQLConnection> {
    private final C namedSqlConnection;

    public AbstractNamedAction(C namedSqlConnection) {
        this.namedSqlConnection = namedSqlConnection;
    }

    public final C getNamedSqlConnection() {
        return namedSqlConnection;
    }
}
