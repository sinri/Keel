package io.github.sinri.keel.test.mysql.jdbc;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.jdbc.KeelJDBCForMySQL;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.github.sinri.keel.test.SharedTestBootstrap;

public class JDBCTest1 {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();
        KeelJDBCForMySQL mySqlJDBC = SharedTestBootstrap.getMySqlJDBC();

        String sqlForSelection = "select test_record_id,name,value as renamed_value,status,record_time from java_test_for_sinri";
        ResultMatrix resultMatrix = mySqlJDBC.queryForSelection(sqlForSelection);
        int totalFetchedRows = resultMatrix.getTotalFetchedRows();
        for (var row : resultMatrix.getRowList()) {
            Keel.outputLogger("JDBCTest1").info("row", row);
        }
    }
}
