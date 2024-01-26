package io.github.sinri.keel.mysql;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.KeelConfiguration;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

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
    private final @Nonnull String dataSourceName;

    public KeelMySQLConfiguration(@Nonnull String dataSourceName, @Nonnull KeelConfiguration keelConfiguration) {
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

    @Nonnull
    public static KeelMySQLConfiguration loadConfigurationForDataSource(@Nonnull KeelConfiguration keelConfiguration, @Nonnull String dataSourceName) {
        KeelConfiguration configuration = keelConfiguration.extract("mysql", dataSourceName);
        return new KeelMySQLConfiguration(dataSourceName, configuration);
    }

    @Nonnull
    public MySQLConnectOptions getConnectOptions() {
        // mysql.XXX.connect::database,host,password,port,user,charset,useAffectedRows,connectionTimeout
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

    @Nonnull
    public PoolOptions getPoolOptions() {
        // mysql.XXX.pool::poolConnectionTimeout
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
        poolOptions.setShared(getPoolShared());
        poolOptions.setName("Keel-MySQL-Pool-" + this.getDataSourceName());
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
        return Objects.requireNonNullElse(schema, "");
    }

    public String getCharset() {
        return readString("charset");
    }

    public Integer getPoolMaxSize() {
        return readAsInteger("poolMaxSize");
    }

    @Nonnull
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

    /**
     * @since 3.0.9
     * You can share a pool between multiple verticles or instances of the same verticle.
     * Such pool should be created outside a verticle otherwise it will be closed when the verticle
     * that created it is undeployed.
     */
    public boolean getPoolShared() {
        return !("NO".equals(readString("poolShared")));
    }


    /**
     * With Client to run SQL on target MySQL Database one-time.
     * The client is to be created, and then soon closed after the sql queried.
     *
     * @since 3.1.6
     */
    @TechnicalPreview(since = "3.1.6")
    public Future<ResultMatrix> instantQuery(String sql) {
        var sqlClient = MySQLBuilder.client()
                .with(this.getPoolOptions())
                .connectingTo(this.getConnectOptions())
                .using(Keel.getVertx())
                .build();
        return Future.succeededFuture()
                .compose(v -> sqlClient.preparedQuery(sql)
                        .execute()
                        .compose(rows -> {
                            return Future.succeededFuture(ResultMatrix.create(rows));
                        }))
                .andThen(ar -> sqlClient.close());
    }
}
