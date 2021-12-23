package io.github.sinri.keel.test.mysql;

import io.github.sinri.keel.mysql.statement.UpdateStatement;

import java.util.List;

public class UpdateBuilderTest {
    public static void main(String[] args) {
        UpdateStatement update = new UpdateStatement();
        update.table("d", "t")
                .setWithValue("a", "b\nc")
                .setWithExpression("b", "abs(c)")
                .whereForAmongst(amongstCondition -> amongstCondition.elementAsExpression("c").amongstValue(List.of(3, "4")))
                .orderByDesc("b")
                .limit(4);
        System.out.println(update);
    }
}
