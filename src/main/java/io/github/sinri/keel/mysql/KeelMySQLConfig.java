package io.github.sinri.keel.mysql;

import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;

import java.util.Map;

public class KeelMySQLConfig {
    protected final String dataSourceName;
    protected final String host;
    protected final int port;
    protected final String username;
    protected final String password;
    protected final String schema;
    protected final String charset;
    protected final boolean useAffectedRows;

    protected final int poolMaxSize;

    public KeelMySQLConfig(String dataSourceName, Map<String, String> map) {
        this.dataSourceName = dataSourceName;
        this.host = map.get("host");
        this.port = Integer.parseInt(map.getOrDefault("host", "3306"));
        this.username = map.get("username");
        this.password = map.get("password");
        this.schema = map.get("schema");
        this.charset = map.get("charset");
        this.useAffectedRows = !"false".equalsIgnoreCase(map.get("useAffectedRows"));

        this.poolMaxSize = Integer.parseInt(map.getOrDefault("pool.maxSize", "10"));
    }

    public KeelMySQLConfig(String dataSourceName, KeelPropertiesReader reader) {
        this.dataSourceName = dataSourceName;
        String prefix = "mysql." + dataSourceName + ".";

        this.host = reader.getProperty(prefix + "host");
        this.port = Integer.parseInt(reader.getProperty(prefix + "port", "3306"));
        this.username = reader.getProperty(prefix + "username");
        this.password = reader.getProperty(prefix + "password");
        this.schema = reader.getProperty(prefix + "schema");
        this.charset = reader.getProperty(prefix + "charset");
        this.useAffectedRows = !"false".equalsIgnoreCase(reader.getProperty(prefix + "useAffectedRows"));

        this.poolMaxSize = Integer.parseInt(reader.getProperty(prefix + "pool.maxSize", "10"));
    }

    public MySQLConnectOptions buildMySQLConnectOptions() {
        return new MySQLConnectOptions()
                .setPort(port)
                .setHost(host)
                .setDatabase(schema)
                .setUser(username)
                .setPassword(password)
                .setCharset(charset)
                .setUseAffectedRows(useAffectedRows);
    }

    public PoolOptions buildPoolOptions() {
        return new PoolOptions().setMaxSize(this.poolMaxSize);
    }

}
