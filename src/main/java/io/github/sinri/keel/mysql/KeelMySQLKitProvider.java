package io.github.sinri.keel.mysql;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 3.0.0
 */
public class KeelMySQLKitProvider {
    private final Map<String, KeelMySQLKit> mysqlKitMap = new ConcurrentHashMap<>();

    public KeelMySQLKit getMySQLKit(String dataSourceName) {
        if (!mysqlKitMap.containsKey(dataSourceName)) {
            KeelMySQLOptions keelMySQLOptions = KeelMySQLOptions.generateOptionsForDataSourceWithPropertiesReader(dataSourceName);
            KeelMySQLKit keelMySQLKit = new KeelMySQLKit(keelMySQLOptions);
            mysqlKitMap.put(dataSourceName, keelMySQLKit);
        }
        return mysqlKitMap.get(dataSourceName);
    }
}
