package io.github.sinri.keel.mysql;

import io.github.sinri.keel.facade.KeelConfiguration;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
public class KeelMySQLConfiguration extends KeelConfiguration {
    private final String dataSourceName;

    public KeelMySQLConfiguration(String dataSourceName, KeelConfiguration keelConfiguration) {
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

    public static @Nullable KeelMySQLConfiguration loadConfigurationForDataSource(@Nonnull KeelConfiguration keelConfiguration, @Nonnull String dataSourceName) {
        KeelConfiguration configuration = keelConfiguration.extract("mysql", dataSourceName);
        if (configuration == null) return null;
        return new KeelMySQLConfiguration(dataSourceName, configuration);
    }

    public static void main(String[] args) {
        MySQLConnectOptions mySQLConnectOptions = new MySQLConnectOptions()
                .setHost("H")
                .setPort(3306)
//                .setCharset("utf8")
                .setDatabase("D")
                .setUser("U")
                .setPassword("P");
        System.out.println("mySQLConnectOptions: " + mySQLConnectOptions.toJson().encodePrettily());

        PoolOptions poolOptions = new PoolOptions()
                .setConnectionTimeout(2)
//                .setConnectionTimeoutUnit(TimeUnit.SECONDS)
                ;
        System.out.println("poolOptions: " + poolOptions.toJson().encodePrettily());

    }

    public MySQLConnectOptions getConnectOptions() {
        // mysql.XXX.connect::database,host,password,port,user,charset,useAffectedRows
        MySQLConnectOptions mySQLConnectOptions = new MySQLConnectOptions()
                .setUseAffectedRows(true);
        mySQLConnectOptions.setHost(getHost())
                .setPort(getPort())
                .setUser(getUsername())
                .setPassword(getPassword());
        String charset = getCharset();
        if (charset != null) mySQLConnectOptions.setCharset(charset);
        String schema = getDatabase();
        if (schema != null) {
            mySQLConnectOptions.setDatabase(schema);
        }

        Integer connectionTimeout = getConnectionTimeout();
        if (connectionTimeout != null) {
            mySQLConnectOptions.setConnectTimeout(connectionTimeout);
        }

        return mySQLConnectOptions;
    }

    public PoolOptions getPoolOptions() {
        // mysql.XXX.pool::connectionTimeout
        PoolOptions poolOptions = new PoolOptions();
        Integer poolMaxSize = getPoolMaxSize();
        if (poolMaxSize != null) {
            poolOptions.setMaxSize(poolMaxSize);
        }
        Integer poolConnectionTimeout = getPoolConnectionTimeout();
        if (poolConnectionTimeout != null) {
            poolOptions.setConnectionTimeout(poolConnectionTimeout);
            poolOptions.setConnectionTimeoutUnit(TimeUnit.SECONDS);
        }
        return poolOptions;
    }

    public String getHost() {
        return readString("host");
    }

    public Integer getPort() {
        return Objects.requireNonNullElse(readAsInteger("port"), 3306);
    }

    public String getPassword() {
        return readString("password");
    }

    public String getUsername() {
        var u = readString("username");
        if (u == null) {
            u = readString("user");
        }
        return u;
    }

    public String getDatabase() {
        String schema = readString("schema");
        if (schema == null) {
            schema = readString("database");
        }
        return schema;
    }

    public String getCharset() {
        return readString("charset");
    }

    public Integer getPoolMaxSize() {
        return readAsInteger("poolMaxSize");
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * The default value of connect timeout = 60000 ms
     *
     * @return connectTimeout - connect timeout, in ms
     * @since 3.0.1 let it be its original setting!
     */
    private Integer getConnectionTimeout() {
        return readAsInteger("connectionTimeout");
    }

    /**
     * Set the amount of time a client will wait for a connection from the pool.
     * If the time is exceeded without a connection available, an exception is provided.
     * TimeUnit would be set by `setConnectionTimeoutUnit`
     *
     * @see <a href="https://vertx.io/docs/apidocs/io/vertx/sqlclient/PoolOptions.html#setConnectionTimeout-int-">...</a>
     */
    public Integer getPoolConnectionTimeout() {
        return readAsInteger("poolConnectionTimeout");
    }
}
