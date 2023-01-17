package io.github.sinri.keel.test.lab.mysql.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Table {
    String tableName();

    List<TableColumn> columns();

    default Map<String, TableColumn> columnMap() {
        Map<String, TableColumn> map = new HashMap<>();
        columns().forEach(column -> {
            map.put(column.getColumn(), column);
        });
        return map;
    }
}
