package io.github.sinri.Keel.test.mysql;

import io.github.sinri.Keel.mysql.SQLBuilder.KeelMySQLUpdateSQLBuilder;

import java.util.List;

public class UpdateBuilderTest {
    public static void main(String[] args) {
        KeelMySQLUpdateSQLBuilder update = new KeelMySQLUpdateSQLBuilder();
        update.table("d", "t")
                .setWithValue("a", "b\nc")
                .setWithExpression("b", "abs(c)")
                .whereForAmongst(amongstCondition -> amongstCondition.elementAsExpression("c").amongstValue(List.of(3, "4")))
                .orderByDesc("b")
                .limit(4);
        System.out.println(update);
    }
}
