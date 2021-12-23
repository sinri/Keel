package io.github.sinri.keel.test.mysql;

import io.github.sinri.keel.mysql.statement.WriteIntoStatement;

import java.util.List;

public class WriteIntoBuilderTest {
    public static void main(String[] args) {
        WriteIntoStatement insert = new WriteIntoStatement();
        insert.intoTable("d1", "t2")
                .columns(List.of("a", "b", "c"))
                .addDataMatrix(List.of(List.of(1, 2, 3), List.of(4, 5, 6)))
                .onDuplicateKeyUpdate("a", "a+1");
        System.out.println(insert);
    }
}
