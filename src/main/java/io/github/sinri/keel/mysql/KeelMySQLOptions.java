package io.github.sinri.keel.mysql;

import io.github.sinri.keel.core.properties.KeelOptions;
import io.vertx.core.net.ClientOptionsBase;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;

public class KeelMySQLOptions extends KeelOptions {
    // TODO use KeelConfiguration
    public String host;
    public int port;
    public String username;
    public String password;
    public String schema;
    public String charset;
    public boolean useAffectedRows;
    /**
     * @since 2.8
     * The default value of connect timeout = 60000 ms = 60s
     */
    public int connectionTimeout = ClientOptionsBase.DEFAULT_CONNECT_TIMEOUT;
    public int poolMaxSize;
    public boolean poolShared;
    /**
     * @since 2.8
     */
    public int poolConnectionTimeout = 30;// sec;
    /**
     * @since 2.8
     */
    public int poolEventLoopSize = 0;// sec; 0 -> reuse current event-loop
    /**
     * @since 2.8
     */
    public int poolIdleTimeout = 0; // sec; 0 -> no timeout
    public boolean allowPublicKeyRetrieval;
    protected String dataSourceName;
    /**
     * If you want to connect to Aliyun PolarDB MySQL, must set it to YES(Boolean as true).
     *
     * @since 2.9
     */
    public boolean tcpKeepAlive;

    public KeelMySQLOptions() {
        this.host = "127.0.0.1";
        this.port = 3306;
        this.username = "anonymous";
        this.password = "";
        this.schema = "test";
        this.charset = "utf8";
        this.useAffectedRows = true;
        this.allowPublicKeyRetrieval = false;
        this.poolMaxSize = 128;
        this.poolShared = false;
        this.tcpKeepAlive=false;
    }

    public static KeelMySQLOptions generateOptionsForDataSourceWithPropertiesReader(String dataSourceName) {
        KeelMySQLOptions keelMySQLOptions = Keel.getPropertiesReader().filter("mysql." + dataSourceName).toConfiguration(KeelMySQLOptions.class);
        keelMySQLOptions.setDataSourceName(dataSourceName);
        return keelMySQLOptions;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public KeelMySQLOptions setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        return this;
    }

    public MySQLConnectOptions buildMySQLConnectOptions() {
        return new MySQLConnectOptions()
                .setPort(port)
                .setHost(host)
                .setDatabase(schema)
                .setUser(username)
                .setPassword(password)
                .setCharset(charset)
                .setUseAffectedRows(useAffectedRows)
                .setConnectTimeout(connectionTimeout)
                .setTcpKeepAlive(tcpKeepAlive);
    }

    public PoolOptions buildPoolOptions() {
        return new PoolOptions()
                .setMaxSize(this.poolMaxSize)
                .setShared(this.poolShared)
                .setConnectionTimeout(this.poolConnectionTimeout)
                .setEventLoopSize(this.poolEventLoopSize)
                .setIdleTimeout(this.poolIdleTimeout)
                ;
    }

    public String buildJDBCConnectionString() {
        return "jdbc:mysql://" + host + ":" + port + "/" + schema
                + "?useSSL=false&useUnicode=true" +
                "&characterEncoding=" + charset
                + "&allowPublicKeyRetrieval=" + (allowPublicKeyRetrieval ? "true" : "false");
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
