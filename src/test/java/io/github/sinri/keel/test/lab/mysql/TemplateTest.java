package io.github.sinri.keel.test.lab.mysql;

import io.github.sinri.keel.mysql.statement.templated.TemplatedStatement;

import java.util.List;

public class TemplateTest {
    public static void main(String[] args) {
        var readStatement = TemplatedStatement.loadTemplateToRead("SqlTemplate/read-1.sql");
        System.out.println("TEMPLATE");
        System.out.println(readStatement);
        System.out.println("=========");
        readStatement.bindArguments(templateArgumentMapping -> templateArgumentMapping
                .bindNull("null_comment")
                .bindExpression("p_expression", "Martin")
                .bindString("p_string", "Biantai('so called \"HenTai\"')")
                .bindStrings("p_array_of_string", List.of("A", "B"))
                .bindNumbers("p_array_if_int", List.of(5, 6.6))
        );
        System.out.println("BUILT");
        System.out.println(readStatement);
    }
}
