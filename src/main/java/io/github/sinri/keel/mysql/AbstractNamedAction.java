package io.github.sinri.keel.mysql;

import io.github.sinri.keel.core.TechnicalPreview;

@TechnicalPreview(since = "3.0.11")
public class AbstractNamedAction<C extends NamedMySQLConnection> {
    private final C namedSqlConnection;

    public AbstractNamedAction(C namedSqlConnection) {
        this.namedSqlConnection = namedSqlConnection;
    }

    public final C getNamedSqlConnection() {
        return namedSqlConnection;
    }
}
