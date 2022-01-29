package io.github.sinri.keel.mysql.matrix;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.data.Numeric;

import java.util.List;

/**
 * @since 1.1
 * @since 1.8 becomes interface
 * May overrides this class to get Customized Data Matrix
 */
public interface ResultMatrix {

    List<JsonObject> getRowList();

    int getTotalFetchedRows();

    int getTotalAffectedRows();

    long getLastInsertedID();

    JsonArray toJsonArray();

    JsonObject getFirstRow();

    JsonObject getRowByIndex(int index);

    String getOneColumnOfFirstRowAsString(String columnName);

    Numeric getOneColumnOfFirstRowAsNumeric(String columnName);

    Integer getOneColumnOfFirstRowAsInteger(String columnName);

    Long getOneColumnOfFirstRowAsLong(String columnName);

    List<String> getOneColumnAsString(String columnName);

    List<Numeric> getOneColumnAsNumeric(String columnName);

    List<Long> getOneColumnAsLong(String columnName);

    List<Integer> getOneColumnAsInteger(String columnName);
}
