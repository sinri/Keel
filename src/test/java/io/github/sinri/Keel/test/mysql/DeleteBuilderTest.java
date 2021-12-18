package io.github.sinri.Keel.test.mysql;

import io.github.sinri.Keel.mysql.SQLBuilder.KeelMySQLDeleteSQLBuilder;
import io.github.sinri.Keel.mysql.condition.CompareCondition;
import io.github.sinri.Keel.mysql.condition.RawCondition;

public class DeleteBuilderTest {
    public static void main(String[] args) {
        KeelMySQLDeleteSQLBuilder delete = new KeelMySQLDeleteSQLBuilder();
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
