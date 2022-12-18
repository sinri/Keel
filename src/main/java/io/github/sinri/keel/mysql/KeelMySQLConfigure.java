package io.github.sinri.keel.mysql;

import io.github.sinri.keel.facade.KeelConfiguration;
import io.github.sinri.keel.facade.KeelConfigurationImpl;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * KeelMySQLConfigure for connections and pool.
 * Commonly,
 * charset = "utf8";
 * useAffectedRows = true;
 * allowPublicKeyRetrieval = false;
 * poolMaxSize = 128;
 * poolShared = false;
 * tcpKeepAlive=false;
 */
public class KeelMySQLConfigure extends KeelConfigurationImpl {
    private final String dataSourceName;

    public KeelMySQLConfigure(String dataSourceName, KeelConfiguration keelConfiguration) {
        this.dataSourceName = dataSourceName;
        this.reloadDataFromJsonObject(keelConfiguration.toJsonObject());
    }

//    public String buildJDBCConnectionString() {
//        MySQLConnectOptions connectOptions = getConnectOptions();
//        var host = connectOptions.getHost();
//        var port = connectOptions.getPort();
//        var schema = connectOptions.getDatabase();
//        var charset = connectOptions.getCharset();
//        boolean ssl = connectOptions.isSsl();
//        return "jdbc:mysql://" + host + ":" + port + "/" + schema
//                + "?useSSL=" + (ssl ? "true" : "false") + "&useUnicode=true" +
//                "&characterEncoding=" + charset
//                + "&allowPublicKeyRetrieval="+(ssl ? "true" : "false");
//    }

    public static @Nullable KeelMySQLConfigure loadConfigurationForDataSource(@Nonnull KeelConfiguration keelConfiguration, @Nonnull String dataSourceName) {
        KeelConfiguration configuration = keelConfiguration.extract("mysql", dataSourceName);
        if (configuration == null) return null;
        return new KeelMySQLConfigure(dataSourceName, configuration);
    }

    public MySQLConnectOptions getConnectOptions() {
        return new MySQLConnectOptions(readJsonObject("connect"));
    }

    public PoolOptions getPoolOptions() {
        return new PoolOptions(readJsonObject("pool"));
    }

    public String getUsername() {
        return readString("username");
    }

    public String getPassword() {
        return readString("password");
    }

    public String getDataSourceName() {
        return dataSourceName;
    }
}
