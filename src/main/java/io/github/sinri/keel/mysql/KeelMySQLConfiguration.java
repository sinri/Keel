package io.github.sinri.keel.mysql;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.facade.configuration.KeelConfigElement;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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
public class KeelMySQLConfiguration extends KeelConfigElement {
    //private final @Nonnull String dataSourceName;

    public KeelMySQLConfiguration(@Nonnull KeelConfigElement base) {
        super(base);
    }


    @Nonnull
    public static KeelMySQLConfiguration loadConfigurationForDataSource(@Nonnull KeelConfigElement configCenter, @Nonnull String dataSourceName) {
        KeelConfigElement keelConfigElement = configCenter.extract("mysql", dataSourceName);
        return new KeelMySQLConfiguration(Objects.requireNonNull(keelConfigElement));
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
        return readString(List.of("host"), null);
    }

    public Integer getPort() {
        return readInteger(List.of("port"), 3306);
    }

    public String getPassword() {
        return readString(List.of("password"), null);
    }

    public String getUsername() {
        var u = readString("username", null);
        if (u == null) {
            u = readString("user", null);
        }
        return u;
    }

    public String getDatabase() {
        String schema = readString("schema", null);
        if (schema == null) {
            schema = readString("database", null);
        }
        return Objects.requireNonNullElse(schema, "");
    }

    public String getCharset() {
        return readString("charset", null);
    }

    public Integer getPoolMaxSize() {
        var x = getChild("poolMaxSize");
        if (x == null) return null;
        return x.getValueAsInteger();
    }

    /**
     * This data source name would be used in MySQL client pool name.
     * Use different name for actually different data sources;
     * if you want to create a temporary data source to perform instant query, UUID is a good component.
     */
    @Nonnull
    public String getDataSourceName() {
        return getName();
    }

    /**
     * The default value of connect timeout = 60000 ms
     *
     * @return connectTimeout - connect timeout, in ms
     * @since 3.0.1 let it be its original setting!
     */
    private Integer getConnectionTimeout() {
        var x = getChild("connectionTimeout");
        if (x == null) {
            return null;
        }
        return x.getValueAsInteger();
    }

    /**
     * Set the amount of time a client will wait for a connection from the pool.
     * If the time is exceeded without a connection available, an exception is provided.
     * TimeUnit would be set by `setConnectionTimeoutUnit`
     *
     * @see <a
     *         href="https://vertx.io/docs/apidocs/io/vertx/sqlclient/PoolOptions.html#setConnectionTimeout-int-">...</a>
     */
    public Integer getPoolConnectionTimeout() {
        KeelConfigElement keelConfigElement = extract("poolConnectionTimeout");
        if (keelConfigElement == null) {
            return null;
        }
        return keelConfigElement.getValueAsInteger();
    }

    /**
     * @since 3.0.9
     *         You can share a pool between multiple verticles or instances of the same verticle.
     *         Such pool should be created outside a verticle otherwise it will be closed when the verticle
     *         that created it is undeployed.
     */
    public boolean getPoolShared() {
        return readBoolean("poolShared", true);
    }


    /**
     * With Client to run SQL on target MySQL Database one-time.
     * The client is to be created, and then soon closed after the sql queried.
     * To use this method safely, remember to enable POOL SHARING and set a unique name for the pool.
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
        // System.out.println("sqlClient: "+sqlClient.getClass()); // class io.vertx.mysqlclient.impl.MySQLPoolImpl
        return Future.succeededFuture()
                     .compose(v -> sqlClient.preparedQuery(sql).execute()
                                            .compose(rows -> {
                                                return Future.succeededFuture(ResultMatrix.create(rows));
                                            }))
                     .andThen(ar -> sqlClient.close());
    }

    /**
     * Handle every batch of rows read, or throw any exceptions in rows handler to stop the process.
     * All dynamic resources would be closed inside this function.
     *
     * @param sql                a sql to be executed and read its result in stream.
     * @param readWindowSize     how many rows read once
     * @param readWindowFunction the async handler of the read rows
     * @since 3.2.21
     */
    @TechnicalPreview(since = "3.2.21")
    public Future<Void> instantQueryForStream(String sql, int readWindowSize, Function<RowSet<Row>, Future<Void>> readWindowFunction) {
        return Future.succeededFuture()
                     .compose(v -> {
                         Pool pool = MySQLBuilder.pool()
                                                 .with(this.getPoolOptions())
                                                 .connectingTo(this.getConnectOptions())
                                                 .using(Keel.getVertx())
                                                 .build();
                         return Future.succeededFuture(pool);
                     })
                     .compose(pool -> {
                         return pool.getConnection()
                                    .compose(sqlConnection -> {
                                        return sqlConnection.prepare(sql)
                                                            .compose(preparedStatement -> {
                                                                Cursor cursor = preparedStatement.cursor();
                                                                return KeelAsyncKit.repeatedlyCall(routineResult -> {
                                                                                       return cursor.read(readWindowSize)
                                                                                                    .compose(readWindowFunction)
                                                                                                    .compose(v -> {
                                                                                                        if (!cursor.hasMore()) {
                                                                                                            routineResult.stop();
                                                                                                            return Future.succeededFuture();
                                                                                                        }
                                                                                                        return Future.succeededFuture();
                                                                                                    });
                                                                                   })
                                                                                   .eventually(() -> cursor.close());
                                                            })
                                                            .eventually(() -> sqlConnection.close());
                                    })
                                    .eventually(() -> pool.close());
                     });
    }
}
