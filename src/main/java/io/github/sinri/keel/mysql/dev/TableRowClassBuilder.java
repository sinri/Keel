package io.github.sinri.keel.mysql.dev;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.0.15
 * @since 3.0.18 Finished Technical Preview.
 */
class TableRowClassBuilder {
    private final @Nonnull String packageName;
    private final @Nullable String schema;
    private final @Nonnull String table;

    private boolean provideConstSchema = true;
    private boolean provideConstTable = true;
    private boolean provideConstSchemaAndTable = false;

    private @Nullable String tableComment;
    private @Nullable String ddl;

    private final @Nonnull List<TableRowClassField> fields = new ArrayList<>();

    public TableRowClassBuilder(@Nonnull String table, @Nullable String schema, @Nonnull String packageName) {
        this.table = table;
        this.schema = schema;
        this.packageName = packageName;
    }

    public TableRowClassBuilder setProvideConstSchema(boolean provideConstSchema) {
        this.provideConstSchema = provideConstSchema;
        return this;
    }

    public TableRowClassBuilder setProvideConstTable(boolean provideConstTable) {
        this.provideConstTable = provideConstTable;
        return this;
    }

    public TableRowClassBuilder setProvideConstSchemaAndTable(boolean provideConstSchemaAndTable) {
        this.provideConstSchemaAndTable = provideConstSchemaAndTable;
        return this;
    }

    public TableRowClassBuilder setDdl(@Nullable String ddl) {
        this.ddl = ddl;
        return this;
    }

    public TableRowClassBuilder setTableComment(@Nullable String tableComment) {
        this.tableComment = tableComment;
        return this;
    }

    public TableRowClassBuilder addField(TableRowClassField field) {
        this.fields.add(field);
        return this;
    }

    public TableRowClassBuilder addFields(@Nonnull List<TableRowClassField> fields) {
        this.fields.addAll(fields);
        return this;
    }

    protected String parsedTableComment() {
        if (tableComment == null || tableComment.isEmpty() || tableComment.isBlank()) {
            return "Table comment is empty.";
        } else {
            return KeelHelpers.stringHelper().escapeForHttpEntity(tableComment);
        }
    }

    public String getClassName() {
        return KeelHelpers.stringHelper().fromUnderScoreCaseToCamelCase(table) + "TableRow";
    }

    public String build() {
        var className = getClassName();
        StringBuilder code = new StringBuilder();

        code
                .append("package ").append(packageName).append(";").append("\n")
                .append("import io.github.sinri.keel.mysql.matrix.AbstractTableRow;\n")
                .append("import io.vertx.core.json.JsonObject;\n")
                .append("\n")
                .append("/**\n")
                .append(" * ").append(parsedTableComment()).append("\n")
                .append(" * (´^ω^`)\n");
        if (schema != null && !schema.isEmpty() && !schema.isBlank()) {
            code.append(" * SCHEMA: ").append(schema).append("\n");
        }
        code
                .append(" * TABLE: ").append(table).append("\n")
                .append(" * (*￣∇￣*)\n")
                .append(" * NOTICE BY KEEL:\n")
                .append(" * \tTo avoid being rewritten, do not modify this file manually, unless editable confirmed.\n")
                .append(" * \tIt was auto-generated on ").append(new Date()).append(".\n")
                .append(" * @see ").append(TableRowClassSourceCodeGenerator.class.getName()).append("\n")
                .append(" */\n")
                .append("public class ").append(className).append(" extends AbstractTableRow {").append("\n");

        if (this.schema != null && !this.schema.isEmpty() && !this.schema.isBlank()) {
            if (this.provideConstSchema) {
                code.append("\tpublic static final String SCHEMA = \"").append(schema).append("\";\n");
            }
            if (this.provideConstSchemaAndTable) {
                code.append("\tpublic static final String SCHEMA_AND_TABLE = \"").append(schema).append(".").append(table).append("\";\n");
            }
        }
        if (this.provideConstTable) {
            code.append("\tpublic static final String TABLE = \"").append(table).append("\";\n");
        }

        code
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
            code.append("\tpublic String sourceSchemaName(){\n")
                    .append("\t\treturn ").append(this.provideConstSchema ? "SCHEMA" : "\"" + schema + "\"").append(";\n")
                    .append("\t}\n");
        }

        fields.forEach(field -> {
            code.append(field.toString()).append("\n");
        });

        code.append("\n}\n");
        if (ddl != null) {
            code.append("\n/*\n").append(ddl).append("\n */\n");
        }

        return code.toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
