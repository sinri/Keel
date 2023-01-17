package io.github.sinri.keel.test.lab.mysql.table;

import io.github.sinri.keel.mysql.KeelMySQLDataSourceProvider;
import io.github.sinri.keel.mysql.statement.SelectStatement;

public class Scenario1 {
    public static void main(String[] args) {
        TableProduct tableProduct = new TableProduct();

        var select = new SelectStatement()
                .columnAsExpression(tableProduct.getColumnProductName().getColumn())
                .from(tableProduct.tableName())
                .where(conditionsComponent -> conditionsComponent
                        .quickMapping(tableProduct.getColumnProductLimit().getColumn(), "2"));
        System.out.println("sql: " + select.toString());

        KeelMySQLDataSourceProvider.getMySQLDataSource().withConnection(sqlConnection -> {
                    return select.queryForRowList(sqlConnection, TableProduct.Row.class);
                })
                .onSuccess(rows -> {
                    rows.forEach(row -> {
                        row.getProductName();
                    });
                });
    }
}
