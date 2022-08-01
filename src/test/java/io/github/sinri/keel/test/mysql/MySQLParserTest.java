package io.github.sinri.keel.test.mysql;

import io.github.sinri.keel.test.SharedTestBootstrap;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;

public class MySQLParserTest {

    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

//        test1();
        test2();
    }

    private static void test1() {
        //Simple SQL parsing
        try {
            Statement stmt = CCJSqlParserUtil.parse(
                    "SELECT * FROM s1.tab1 where a=1; -- uuu"
            );
            stmt.accept(new TestStatementVisitor());
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

    private static void test2() {
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(
                    "SELECT * FROM tab1; " +
                            "SELECT * FROM tab2;" +
                            "ALTER TABLE oms.`merchant`\n" +
                            "ADD COLUMN `default_org_type`  varchar(10) DEFAULT NULL COMMENT '库存组织初始值配置';" +
                            "truncate table s.t;"
            );
            //statements.accept(new TestStatementVisitor());
            statements.getStatements().forEach(statement -> {
                statement.accept(new TestStatementVisitor());
            });
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
}
