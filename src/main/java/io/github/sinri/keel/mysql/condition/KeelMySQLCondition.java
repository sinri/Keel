package io.github.sinri.keel.mysql.condition;

import io.github.sinri.keel.mysql.exception.KeelSQLGenerateError;

/**
 * @since 2.8 became interface
 */
public interface KeelMySQLCondition {
    /**
     * 生成SQL的条件表达式文本。如果出错，则抛出 KeelSQLGenerateError 异常。
     *
     * @return The generated SQL component as String
     * @throws KeelSQLGenerateError when the sql component could not be generated correctly
     */
    String toString();
}