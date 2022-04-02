package io.github.sinri.keel.test.v1.mysql;

import io.github.sinri.keel.mysql.condition.CompareCondition;
import io.github.sinri.keel.mysql.statement.UpdateStatement;

import java.util.List;

public class UpdateBuilderTest {
    public static void main(String[] args) {
        UpdateStatement update = new UpdateStatement();
        update.table("d", "t")
                .setWithValue("a", "b\nc")
                .setWithExpression("b", "abs(c)")
                .where(
                        conditionsComponent -> conditionsComponent
                                .among(amongstCondition -> amongstCondition.element("c").amongst(List.of(3, "4")))
                                .comparison(CompareCondition.OP_EQ, compareCondition -> compareCondition.compare("e").against(123.675))
                )
                .orderByDesc("b")
                .limit(4);
        System.out.println(update);
    }
}
