package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureForEach;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.SqlConnection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 用于快速根据数据库中的表结构生成AbstractTableRow的实现类的代码生成工具。
 * 为安全起见，默认不打开覆盖开关。
 *
 * @since 2.8
 */
public class TableRowClassGenerator {

    private final SqlConnection sqlConnection;
    private final Set<String> tableSet;
    private String schema;
    private boolean rewrite;

    public TableRowClassGenerator(SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
        this.schema = null;
        this.tableSet = new HashSet<>();
        this.rewrite = false;
    }

    public TableRowClassGenerator forSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public TableRowClassGenerator forTables(Collection<String> tables) {
        this.tableSet.addAll(tables);
        return this;
    }

    public TableRowClassGenerator forTable(String table) {
        this.tableSet.add(table);
        return this;
    }

    public TableRowClassGenerator setRewrite(boolean rewrite) {
        this.rewrite = rewrite;
        return this;
    }

    public Future<Void> generate(String packageName, String packagePath) {
        return this.confirmTables()
                .compose(tables -> {
                    return generateForTables(packageName, packagePath, tables);
                });
    }

    private Future<Set<String>> confirmTables() {
        Set<String> tables = new HashSet<>();
        if (this.tableSet.isEmpty()) {
            if (schema == null || schema.isEmpty() || schema.isBlank()) {
                return this.sqlConnection.query("show tables")
                        .execute()
                        .compose(rows -> {
                            rows.forEach(row -> {
                                String tableName = row.getString(0);
                                tables.add(tableName);
                            });
                            return Future.succeededFuture(tables);
                        });
            } else {
                return this.sqlConnection.query("show tables in `" + this.schema + "`")
                        .execute()
                        .compose(rows -> {
                            rows.forEach(row -> {
                                String tableName = row.getString(0);
                                tables.add(tableName);
                            });
                            return Future.succeededFuture(tables);
                        });
            }
        } else {
            tables.addAll(this.tableSet);
            return Future.succeededFuture(tables);
        }
    }

    private Future<Void> generateForTables(String packageName, String packagePath, Collection<String> tables) {
        return FutureForEach.call(
                tables,
                table -> {
                    String className = Keel.stringHelper().fromUnderScoreCaseToCamelCase(table) + "TableRow";
//                    className = className.substring(0, 1).toUpperCase() + className.substring(1);
                    String classFile = packagePath + "/" + className + ".java";

                    return Future.succeededFuture()
                            .compose(v -> {
                                if (schema == null || schema.isEmpty() || schema.isBlank()) {
                                    return getTableComment(sqlConnection, table, null);
                                } else {
                                    return getTableComment(sqlConnection, table, schema);
                                }
                            })
                            .compose(table_comment -> {
                                StringBuilder classContent = new StringBuilder();
                                classContent
                                        .append("package ").append(packageName).append(";").append("\n")
                                        .append("import io.github.sinri.keel.mysql.matrix.AbstractTableRow;\n")
                                        .append("import io.vertx.core.Future;\n")
                                        .append("import io.vertx.core.json.JsonObject;\n")
                                        .append("\n")
                                        .append("/**\n");
                                if (table_comment == null || table_comment.isEmpty() || table_comment.isBlank()) {
                                    classContent.append(" * Table ").append(table).append(" has no table comment.\n");
                                } else {
                                    classContent.append(" * ").append(table_comment).append("\n");
                                }
                                classContent.append(" * (´^ω^`)\n");
                                if (schema != null && !schema.isEmpty() && !schema.isBlank()) {
                                    classContent.append(" * SCHEMA: ").append(schema).append("\n");
                                }
                                classContent
                                        .append(" * TABLE: ").append(table).append("\n")
                                        .append(" * (*￣∇￣*)\n")
                                        .append(" * NOTICE BY KEEL:\n")
                                        .append(" * \tTo avoid being rewritten, do not modify this file manually, unless editable confirmed.\n")
                                        .append(" * \tIt was auto-generated on ").append(KeelMySQLKit.nowAsMySQLDatetime()).append(".\n")
                                        .append(" * @see ").append(this.getClass().getName()).append("\n")
                                        .append(" */\n")
                                        .append("public class ").append(className).append(" extends AbstractTableRow {").append("\n");
                                if (this.schema != null && !this.schema.isEmpty() && !this.schema.isBlank()) {
                                    classContent.append("\tpublic static final String SCHEMA = \"").append(schema).append("\";\n");
                                }
                                classContent.append("\n")
                                        .append("\tpublic static final String TABLE = \"").append(table).append("\";\n")
                                        .append("\n")
                                        .append("\t").append("public ").append(className).append("(JsonObject tableRow) {\n")
                                        .append("\t\tsuper(tableRow);\n")
                                        .append("\t}\n")
                                        .append("\n")
                                        .append("\t@Override\n")
                                        .append("\tpublic String sourceTableName() {\n")
                                        .append("\t\treturn TABLE;\n")
                                        .append("\t}\n")
                                        .append("\n");
                                if (this.schema != null && !this.schema.isEmpty() && !this.schema.isBlank()) {
                                    classContent.append("\tpublic String sourceSchemaName(){\n")
                                            .append("\t\treturn SCHEMA;\n")
                                            .append("\t}\n");
                                }

                                String sql_for_columns = "show full columns in ";
                                if (schema != null && !schema.isEmpty() && !schema.isBlank()) {
                                    sql_for_columns += "`" + schema + "`.";
                                }
                                sql_for_columns += "`" + table + "`;";
                                return sqlConnection.query(sql_for_columns)
                                        .execute()
                                        .compose(rows -> {
                                            rows.forEach(row -> {
                                                String field = row.getString("Field");
                                                String type = row.getString("Type");
                                                String comment = row.getString("Comment");

                                                classContent.append(this.buildFieldGetter(field, type, comment)).append("\n");
                                            });
                                            return Future.succeededFuture();
                                        })
                                        .compose(done -> {
                                            String sql_sct = "show create table ";
                                            if (schema != null && !schema.isEmpty() && !schema.isBlank()) {
                                                sql_sct += "`" + schema + "`.";
                                            }
                                            sql_sct += "`" + table + "`;";
                                            return sqlConnection.query(sql_sct)
                                                    .execute()
                                                    .compose(rows -> {
                                                        AtomicReference<String> creation = new AtomicReference<>();
                                                        rows.forEach(row -> {
                                                            creation.set(row.getString(1));
                                                        });
                                                        return Future.succeededFuture(creation.get());
                                                    });
                                        })
                                        .compose(creation -> {
                                            classContent.append("\n}\n");
                                            if (creation != null) {
                                                classContent.append("\n/*\n").append(creation).append("\n */\n");
                                            }
                                            if (this.rewrite) {
                                                return Keel.getVertx().fileSystem().writeFile(classFile, Buffer.buffer(classContent.toString()));
                                            } else {
                                                return Keel.getVertx().fileSystem().exists(classFile)
                                                        .compose(existed -> {
                                                            if (existed) {
                                                                return Keel.getVertx().fileSystem().readFile(classFile)
                                                                        .compose(existedContentBuffer -> {
                                                                            return Keel.getVertx().fileSystem().writeFile(
                                                                                    classFile,
                                                                                    existedContentBuffer.appendString("\n\n").appendString(classContent.toString())
                                                                            );
                                                                        });
                                                            } else {
                                                                return Keel.getVertx().fileSystem().writeFile(classFile, Buffer.buffer(classContent.toString()));
                                                            }
                                                        });
                                            }
                                        });
                            });
                }
        );
    }

    private Future<String> getTableComment(SqlConnection sqlConnection, String table, String schema) {
        String sql_for_table_comment = "SELECT TABLE_COMMENT " +
                "FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_NAME = '" + table + "' " +
                (schema == null ? "" : ("AND TABLE_SCHEMA = '" + schema + "' "));
        return sqlConnection.query(sql_for_table_comment).execute()
                .compose(rows -> {
                    AtomicReference<String> comment = new AtomicReference<>();
                    rows.forEach(row -> {
                        comment.set(row.getString("TABLE_COMMENT"));
                    });
                    return Future.succeededFuture(comment.get());
                });
    }

    private String buildFieldGetter(String field, String type, String comment) {
        String getter = "get" + Keel.stringHelper().fromUnderScoreCaseToCamelCase(field);
        String returnType = "Object";
        String readMethod = "readValue";

        if (type.contains("bigint")) {
            returnType = "Long";
            readMethod = "readLong";
        } else if (type.contains("int")) {
            // tinyint smallint mediumint
            returnType = "Integer";
            readMethod = "readInteger";
        } else if (type.contains("float")) {
            returnType = "Float";
            readMethod = "readFloat";
        } else if (type.contains("double")) {
            returnType = "Double";
            readMethod = "readDouble";
        } else if (type.contains("decimal")) {
            returnType = "Number";
            readMethod = "readNumber";
        } else if (type.contains("datetime") || type.contains("timestamp")) {
            returnType = "String";
            readMethod = "readDateTime";
        } else if (type.contains("date")) {
            returnType = "String";
            readMethod = "readDate";
        } else if (type.contains("time")) {
            returnType = "String";
            readMethod = "readTime";
        } else if (type.contains("text") || type.contains("char")) {
            // mediumtext, varchar, etc.
            returnType = "String";
            readMethod = "readString";
        }

        String s = "\t/*\n";
        if (comment != null && !comment.isEmpty() && !comment.isBlank()) {
            s += "\t * " + comment + "\n\t * \n";
        }
        s += "\t * Field `" + field + "` of type `" + type + "`.\n" +
                "\t */\n" +
                "\tpublic " + returnType + " " + getter + "() {" + "\n" +
                "\t\t" + "return " + readMethod + "(\"" + field + "\");" + "\n" +
                "\t}" + "\n";

        return s;
    }
}
