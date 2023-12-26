package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.mysql.dev.TableRowClassSourceCodeGenerator;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.SqlConnection;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于快速根据数据库中的表结构生成AbstractTableRow的实现类的代码生成工具。
 * 为安全起见，默认不打开覆盖开关。
 *
 * @see TableRowClassSourceCodeGenerator
 * @since 2.8
 * @since 3.0.11 Escape the comment for JAVA API DOC.
 * @deprecated since 3.0.16, use io.github.sinri.keel.mysql.dev.TableRowClassSourceCodeGenerator instead.
 */
@Deprecated(since = "3.0.16")
public class TableRowClassGenerator {

    private static final Pattern patternForLooseEnum;
    private static final Pattern patternForStrictEnum;

    static {
        patternForLooseEnum = Pattern.compile("Enum\\{([A-Za-z0-9_, ]+)}");
        patternForStrictEnum = Pattern.compile("Enum<([A-Za-z0-9_.]+)>");
    }

    private final SqlConnection sqlConnection;
    private final Set<String> tableSet;
    private final Set<String> excludedTableSet;
    private String schema;
    private boolean rewrite;
    /**
     * Generate an Enum in the class and let the getter return the enum.
     * Loose Enum means that, you use a String field in table, but you defined some values in Java as Enum.
     * Values other than the enum defined ones may be treated as null in JAVA.
     */
    private boolean supportLooseEnum = false;
    /**
     * Generate an Enum in the class and let the getter return the enum.
     * Strict Enum means that, an Enum is defined independent, a reference declared here.
     * Values other than the enum defined ones may be treated as null in JAVA.
     */
    private boolean supportStrictEnum = false;
    private String strictEnumPackage = "";

    private boolean provideConstSchema = true;
    private boolean provideConstTable = true;
    private boolean provideConstSchemaAndTable = false;

    public TableRowClassGenerator(SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
        this.schema = null;
        this.tableSet = new HashSet<>();
        this.excludedTableSet = new HashSet<>();
        this.rewrite = false;
    }

    public TableRowClassGenerator forSchema(String schema) {
        if (schema == null || schema.isEmpty() || schema.isBlank()) {
            this.schema = null;
        } else {
            this.schema = schema;
        }
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

    /**
     * @since 3.0.11
     */
    public TableRowClassGenerator excludeTables(Collection<String> tables) {
        this.excludedTableSet.addAll(tables);
        return this;
    }

    public TableRowClassGenerator setRewrite(boolean rewrite) {
        this.rewrite = rewrite;
        return this;
    }

    /**
     * @param supportLooseEnum supportLooseEnum
     * @deprecated since 3.0.15 use
     */
    @Deprecated(since = "3.0.15")
    public TableRowClassGenerator setSupportLooseEnum(boolean supportLooseEnum) {
        this.supportLooseEnum = supportLooseEnum;
        return this;
    }

    /**
     * @since 3.0.15
     */
    public TableRowClassGenerator supportLooseEnum() {
        this.supportLooseEnum = true;
        return this;
    }

    /**
     * @param strictEnumPackage the package name, either ending with a dot, or just empty.
     * @since 3.0.15
     */
    public TableRowClassGenerator supportStrictEnum(String strictEnumPackage) {
        this.supportStrictEnum = true;
        this.strictEnumPackage = strictEnumPackage;
        return this;
    }

    /**
     * @param provideConstSchema
     * @return
     * @since 3.0.10
     */
    public TableRowClassGenerator setProvideConstSchema(boolean provideConstSchema) {
        this.provideConstSchema = provideConstSchema;
        return this;
    }

    /**
     * @param provideConstSchemaAndTable
     * @return
     * @since 3.0.10
     */
    public TableRowClassGenerator setProvideConstSchemaAndTable(boolean provideConstSchemaAndTable) {
        this.provideConstSchemaAndTable = provideConstSchemaAndTable;
        return this;
    }

    /**
     * @param provideConstTable
     * @return
     * @since 3.0.10
     */
    public TableRowClassGenerator setProvideConstTable(boolean provideConstTable) {
        this.provideConstTable = provideConstTable;
        return this;
    }

    public Future<Void> generate(String packageName, String packagePath) {
        return this.confirmTablesToGenerate()
                .compose(tables -> generateForTables(packageName, packagePath, tables));
    }

    private Future<Set<String>> confirmTablesToGenerate() {
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
                            if (!this.excludedTableSet.isEmpty()) {
                                tables.removeAll(this.excludedTableSet);
                            }
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
                            if (!this.excludedTableSet.isEmpty()) {
                                tables.removeAll(this.excludedTableSet);
                            }
                            return Future.succeededFuture(tables);
                        });
            }
        } else {
            tables.addAll(this.tableSet);
            if (!this.excludedTableSet.isEmpty()) {
                tables.removeAll(this.excludedTableSet);
            }
            return Future.succeededFuture(tables);
        }
    }

    private Future<Void> generateForTables(String packageName, String packagePath, Collection<String> tables) {
        return KeelAsyncKit.iterativelyCall(
                tables,
                table -> {
                    String className = KeelHelpers.stringHelper().fromUnderScoreCaseToCamelCase(table) + "TableRow";
                    String classFile = packagePath + "/" + className + ".java";
                    return this.generateClassCodeForOneTable(schema, table, packageName, className)
                            .compose(code -> {
                                if (this.rewrite) {
                                    return Keel.getVertx().fileSystem().writeFile(classFile, Buffer.buffer(code));
                                } else {
                                    return Keel.getVertx().fileSystem().exists(classFile)
                                            .compose(existed -> {
                                                if (existed) {
                                                    return Keel.getVertx().fileSystem().readFile(classFile)
                                                            .compose(existedContentBuffer -> {
                                                                return Keel.getVertx().fileSystem().writeFile(
                                                                        classFile,
                                                                        existedContentBuffer.appendString("\n\n").appendString(code)
                                                                );
                                                            });
                                                } else {
                                                    return Keel.getVertx().fileSystem().writeFile(classFile, Buffer.buffer(code));
                                                }
                                            });
                                }
                            });
                });
    }

    private Future<String> getCommentOfTable(String table, String schema) {
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
        String getter = "get" + KeelHelpers.stringHelper().fromUnderScoreCaseToCamelCase(field);
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

        StringBuilder getter_string = new StringBuilder();

        String enum_name = null;
        if (type.contains("char") && comment != null) {
            if (supportLooseEnum) {
                Matcher matcher = patternForLooseEnum.matcher(comment);
                if (matcher.find()) {
                    String enumValuesString = matcher.group(1);
                    String[] enumValueArray = enumValuesString.split("[, ]+");
                    if (enumValueArray.length > 0) {
                        // to build enum
                        enum_name = KeelHelpers.stringHelper().fromUnderScoreCaseToCamelCase(field) + "Enum";

                        getter_string
                                .append("\t/**\n")
                                .append("\t * Enum for Field `").append(field).append("` \n")
                                .append("\t */\n")
                                .append("\tpublic enum ").append(enum_name).append(" {\n");
                        for (var enumValue : enumValueArray) {
                            getter_string.append("\t\t").append(enumValue).append(",\n");
                        }
                        getter_string.append("\t}\n");
                    }
                }
            }
            if (supportStrictEnum) {
                Matcher matcher = patternForStrictEnum.matcher(comment);
                if (matcher.find()) {
                    String enumClassPathTail = matcher.group(1);
                    String enumClassPath = this.strictEnumPackage + enumClassPathTail;
                    try {
                        Class<?> enumClass = Class.forName(enumClassPath);
                        if (!enumClass.isEnum()) {
                            throw new RuntimeException("Defined Enum Class not enum in Strict Mode");
                        }
                        enum_name = enumClassPath;
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Defined Enum Class not found in Strict Mode", e);
                    }
                }
            }
        }

        if (enum_name == null) {
            getter_string.append("\t/*\n");
            if (comment != null) {
                String escapedComment = KeelHelpers.stringHelper().escapeForHttpEntity(comment);
                getter_string.append("\t * ").append(escapedComment).append("\n\t * \n");
            }
            getter_string.append("\t * Field `").append(field).append("` of type `").append(type).append("`.\n")
                    .append("\t */\n")
                    .append("\tpublic ").append(returnType).append(" ").append(getter).append("() {\n")
                    .append("\t\treturn ").append(readMethod).append("(\"").append(field).append("\");\n")
                    .append("\t}\n");
        } else {
            getter_string.append("\t/*\n")
                    .append("\t * ").append(comment).append("\n\t * \n")
                    .append("\t * Loose Enum of Field `").append(field).append("` of type `").append(type).append("`.\n")
                    .append("\t */\n")
                    .append("\tpublic ").append(enum_name).append(" ").append(getter).append("() {\n")
                    .append("\t\treturn ").append(enum_name).append(".valueOf(\n")
                    .append("\t\t\t").append(readMethod).append("(\"").append(field).append("\")\n")
                    .append("\t\t);\n")
                    .append("\t}\n");

            getter_string.append("\t/*\n")
                    .append("\t * ").append(comment).append("\n\t * \n")
                    .append("\t * Raw value of Field `").append(field).append("` of type `").append(type).append("`.\n")
                    .append("\t */\n")
                    .append("\tpublic ").append(returnType).append(" ").append(getter).append("AsRawString() {\n")
                    .append("\t\treturn ").append(readMethod).append("(\"").append(field).append("\");\n")
                    .append("\t}\n");
        }

        return getter_string.toString();
    }

    private Future<String> buildAllFieldGetters(String table, String schema) {
        String sql_for_columns = "show full columns in ";
        if (schema != null && !schema.isEmpty() && !schema.isBlank()) {
            sql_for_columns += "`" + schema + "`.";
        }
        sql_for_columns += "`" + table + "`;";

        StringBuilder getters = new StringBuilder();

        return sqlConnection.query(sql_for_columns)
                .execute()
                .compose(rows -> {
                    rows.forEach(row -> {
                        String field = row.getString("Field");
                        String type = row.getString("Type");
                        String comment = row.getString("Comment");
                        if (comment == null || comment.isEmpty() || comment.isBlank()) {
                            comment = null;
                        }

                        getters.append(this.buildFieldGetter(field, type, comment)).append("\n");
                    });
                    return Future.succeededFuture();
                })
                .compose(v -> {
                    return Future.succeededFuture(getters.toString());
                });
    }

    private Future<String> getCreationOfTable(String table, String schema) {
        String sql_sct = "show create table ";
        if (schema != null) {
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
    }

    private Future<String> generateClassCodeForOneTable(String schema, String table, String packageName, String className) {
        return Future.all(
                        this.getCommentOfTable(table, schema),// comment of table
                        this.buildAllFieldGetters(table, schema), // getters
                        this.getCreationOfTable(table, schema)// creation
                )
                .compose(compositeFuture -> {
                    String table_comment = compositeFuture.resultAt(0);
                    String getters = compositeFuture.resultAt(1);
                    String creation = compositeFuture.resultAt(2);

                    StringBuilder classContent = new StringBuilder();

                    classContent
                            .append("package ").append(packageName).append(";").append("\n")
                            .append("import io.github.sinri.keel.mysql.matrix.AbstractTableRow;\n")
                            //.append("import io.vertx.core.Future;\n")
                            .append("import io.vertx.core.json.JsonObject;\n")
                            .append("\n")
                            .append("/**\n");
                    if (table_comment == null || table_comment.isEmpty() || table_comment.isBlank()) {
                        classContent.append(" * Table ").append(table).append(" has no table comment.\n");
                    } else {
                        String escapedComment = KeelHelpers.stringHelper().escapeForHttpEntity(table_comment);
                        classContent.append(" * ").append(escapedComment).append("\n");
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
                            .append(" * \tIt was auto-generated on ").append(new Date()).append(".\n")
                            .append(" * @see ").append(this.getClass().getName()).append("\n")
                            .append(" */\n")
                            .append("public class ").append(className).append(" extends AbstractTableRow {").append("\n");
                    if (this.schema != null && !this.schema.isEmpty() && !this.schema.isBlank()) {
                        if (this.provideConstSchema) {
                            classContent.append("\tpublic static final String SCHEMA = \"").append(schema).append("\";\n");
                        }
                        if (this.provideConstSchemaAndTable) {
                            classContent.append("\tpublic static final String SCHEMA_AND_TABLE = \"").append(schema).append(".").append(table).append("\";\n");
                        }
                    }
                    if (this.provideConstTable) {
                        classContent.append("\tpublic static final String TABLE = \"").append(table).append("\";\n");
                    }
                    classContent
                            .append("\n")
                            .append("\t").append("public ").append(className).append("(JsonObject tableRow) {\n")
                            .append("\t\tsuper(tableRow);\n")
                            .append("\t}\n")
                            .append("\n")
                            .append("\t@Override\n")
                            .append("\tpublic String sourceTableName() {\n")
                            .append("\t\treturn ").append(this.provideConstTable ? "TABLE" : "\"" + table + "\"").append(";\n")
                            .append("\t}\n")
                            .append("\n");
                    if (this.schema != null) {
                        classContent.append("\tpublic String sourceSchemaName(){\n")
                                .append("\t\treturn ").append(this.provideConstSchema ? "SCHEMA" : "\"" + schema + "\"").append(";\n")
                                .append("\t}\n");
                    }

                    classContent.append(getters);
                    classContent.append("\n}\n");
                    if (creation != null) {
                        classContent.append("\n/*\n").append(creation).append("\n */\n");
                    }

                    return Future.succeededFuture(classContent.toString());
                });
    }
}
