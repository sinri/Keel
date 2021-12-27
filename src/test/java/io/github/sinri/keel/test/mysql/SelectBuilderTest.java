package io.github.sinri.keel.test.mysql;

import io.github.sinri.keel.mysql.condition.CompareCondition;
import io.github.sinri.keel.mysql.statement.SelectStatement;
import io.github.sinri.keel.mysql.statement.UnionStatement;

import java.util.List;

public class SelectBuilderTest {
    public static void main(String[] args) {
        SelectStatement select = new SelectStatement();
        select
                .column(columnComponent -> columnComponent.field("t1", "id"))
                .columnWithAlias("t2.name", "t2name")
                .columnAsExpression("t1.type")
                .column(columnComponent -> columnComponent.expression("count(*)").alias("total"))
                .from("t1", null)
                .leftJoin(
                        joinComponent -> joinComponent.table("t2")
                                .onForCompare(
                                        keelMySQLCompareCondition -> keelMySQLCompareCondition.compareExpression("t1.id")
                                                .operator(CompareCondition.OP_EQ)
                                                .againstExpression("t2.id")
                                )
                )
                .whereForCompare(keelMySQLCompareCondition -> keelMySQLCompareCondition.compareExpression("t2.name").contains("keyword"))
                .whereForAmongst(amongstCondition -> amongstCondition.elementAsExpression("t2.option").not().amongstValueList(List.of("123", 456)))
                .where(
                        new CompareCondition("t2.name")
                                .contains("keyword")
                )
                .groupBy("t2.name").groupBy("t1.type")
                .having(
                        new CompareCondition(CompareCondition.OP_EQ).compareExpression("total").againstValue(2)
                )
                .limit(5, 5);

        System.out.println(select);

        UnionStatement union = new UnionStatement();
        union.union(select.toString()).unionAll(select.toString());
        System.out.println(union);
    }
}
