package io.github.sinri.keel.test.mysql;

import io.github.sinri.keel.mysql.condition.CompareCondition;
import io.github.sinri.keel.mysql.condition.RawCondition;
import io.github.sinri.keel.mysql.statement.DeleteStatement;

public class DeleteBuilderTest {
    public static void main(String[] args) {
        DeleteStatement delete = new DeleteStatement();
        delete.from("x")
                .whereForOrGroup(
                        groupCondition -> groupCondition
                                .add(new RawCondition("p<b"))
                                .add(new CompareCondition(CompareCondition.OP_EQ).compareExpression("f").againstValue("rfv"))
                )
                .orderByAsc("p")
                .limit(4);
        System.out.println(delete);
    }
}
