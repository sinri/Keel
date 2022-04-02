package io.github.sinri.keel.test.v1.mysql;

import io.github.sinri.keel.mysql.matrix.AbstractTableRow;
import io.vertx.core.json.JsonObject;

public class TableRowJsonTest {
    public static void main(String[] args) {
        C1 c1 = new C1(new JsonObject().put("a", "a").put("b", 1));
        JsonObject jsonObject = new JsonObject().put("c1", c1.getTableRow());


        System.out.println(jsonObject.toString());
    }

    public static class C1 extends AbstractTableRow {
        public C1(JsonObject tableRow) {
            super(tableRow);
        }
    }
}
