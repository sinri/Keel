package io.github.sinri.keel.mysql.dev;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.0.15
 * @since 3.0.18 Finished Technical Preview.
 */
public class TableRowClassSourceCodeGenerator {
    private final SqlConnection sqlConnection;
    private final Set<String> tableSet;
    private final Set<String> excludedTableSet;
    private String schema;

    private boolean provideConstSchema = true;
    private boolean provideConstTable = true;
    private boolean provideConstSchemaAndTable = false;
    private @Nullable String strictEnumPackage = null;
    private @Nullable String envelopePackage = null;

    public TableRowClassSourceCodeGenerator(SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
        this.schema = null;
        this.tableSet = new HashSet<>();
        this.excludedTableSet = new HashSet<>();
    }

    public TableRowClassSourceCodeGenerator(NamedMySQLConnection namedMySQLConnection) {
        this.sqlConnection = namedMySQLConnection.getSqlConnection();
        this.schema = null;
        this.tableSet = new HashSet<>();
        this.excludedTableSet = new HashSet<>();
    }

    public TableRowClassSourceCodeGenerator forSchema(String schema) {
        if (schema == null || schema.isEmpty() || schema.isBlank()) {
            this.schema = null;
        } else {
            this.schema = schema;
        }
        return this;
    }

    public TableRowClassSourceCodeGenerator forTables(Collection<String> tables) {
        this.tableSet.addAll(tables);
        return this;
    }

    public TableRowClassSourceCodeGenerator forTable(String table) {
        this.tableSet.add(table);
        return this;
    }

    public TableRowClassSourceCodeGenerator excludeTables(Collection<String> tables) {
        this.excludedTableSet.addAll(tables);
        return this;
    }

    public TableRowClassSourceCodeGenerator setProvideConstSchema(boolean provideConstSchema) {
        this.provideConstSchema = provideConstSchema;
        return this;
    }

    public TableRowClassSourceCodeGenerator setProvideConstSchemaAndTable(boolean provideConstSchemaAndTable) {
        this.provideConstSchemaAndTable = provideConstSchemaAndTable;
        return this;
    }

    public TableRowClassSourceCodeGenerator setProvideConstTable(boolean provideConstTable) {
        this.provideConstTable = provideConstTable;
        return this;
    }

    /**
     * @param strictEnumPackage empty or a package path. No dot in tail.
     */
    public TableRowClassSourceCodeGenerator setStrictEnumPackage(@Nonnull String strictEnumPackage) {
        this.strictEnumPackage = strictEnumPackage;
        return this;
    }

    /**
     * @param envelopePackage empty or a package path. No dot in tail.
     * @since 3.1.0
     */
    public TableRowClassSourceCodeGenerator setEnvelopePackage(@Nonnull String envelopePackage) {
        this.envelopePackage = envelopePackage;
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
        Map<String, String> writeMap = new HashMap<>();
        return KeelAsyncKit.iterativelyCall(
                        tables,
                        table -> {
                            String className = KeelHelpers.stringHelper().fromUnderScoreCaseToCamelCase(table) + "TableRow";
                            String classFile = packagePath + "/" + className + ".java";
                            return this.generateClassCodeForOneTable(schema, table, packageName, className)
                                    .compose(code -> {
                                        writeMap.put(classFile, code);
                                        return Future.succeededFuture();
                                    });
                        })
                .compose(v -> {
                    return KeelAsyncKit.iterativelyCall(writeMap.entrySet(), entry -> {
                        var classFile = entry.getKey();
                        var code = entry.getValue();
                        return Keel.getVertx().fileSystem().writeFile(classFile, Buffer.buffer(code));
                    });
                });
    }

    private Future<String> generateClassCodeForOneTable(String schema, String table, String packageName, String className) {
        return Future.all(
                        this.getCommentOfTable(table, schema),// comment of table
                        this.getFieldsOfTable(table, schema),// fields
                        this.getCreationOfTable(table, schema)// creation ddl
                )
                .compose(compositeFuture -> {
                    String table_comment = compositeFuture.resultAt(0);
                    List<TableRowClassField> fields = compositeFuture.resultAt(1);
                    String creation = compositeFuture.resultAt(2);

                    String code = new TableRowClassBuilder(table, schema, packageName)
                            .setTableComment(table_comment)
                            .setDdl(creation)
                            .setProvideConstTable(provideConstTable)
                            .setProvideConstSchema(provideConstSchema)
                            .setProvideConstSchemaAndTable(provideConstSchemaAndTable)
                            .addFields(fields)
                            .build();
                    return Future.succeededFuture(code);
                });
    }

    /**
     * Fetch comment of a table (in schema).
     */
    private Future<String> getCommentOfTable(@Nonnull String table, @Nullable String schema) {
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

    private Future<List<TableRowClassField>> getFieldsOfTable(@Nonnull String table, @Nullable String schema) {
        String sql_for_columns = "show full columns in ";
        if (schema != null && !schema.isEmpty() && !schema.isBlank()) {
            sql_for_columns += "`" + schema + "`.";
        }
        sql_for_columns += "`" + table + "`;";

        return sqlConnection.query(sql_for_columns)
                .execute()
                .compose(rows -> {
                    List<TableRowClassField> fields = new ArrayList<>();
                    rows.forEach(row -> {
                        String field = row.getString("Field");
                        String type = row.getString("Type");
                        String comment = row.getString("Comment");
                        if (comment == null || comment.isEmpty() || comment.isBlank()) {
                            comment = null;
                        }

                        // since 3.1.10
                        String nullability = row.getString("Null");
                        boolean isNullable = "YES".equalsIgnoreCase(nullability);

                        fields.add(new TableRowClassField(field, type, isNullable, comment, strictEnumPackage, envelopePackage));
                    });
                    return Future.succeededFuture(fields);
                });
    }

    private Future<String> getCreationOfTable(@Nonnull String table, @Nullable String schema) {
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
}
