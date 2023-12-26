package io.github.sinri.keel.mysql.dev;

import io.github.sinri.keel.helper.KeelHelpers;

import java.util.Collection;

/**
 * @since 3.0.15
 * @since 3.0.18 Finished Technical Preview.
 */
public class TableRowClassFieldLooseEnum {
    private final String fieldName;
    private final Collection<String> enumElements;
    private final String enumName;

    public TableRowClassFieldLooseEnum(String fieldName, Collection<String> enumElements) {
        this.fieldName = fieldName;
        this.enumElements = enumElements;
        this.enumName = KeelHelpers.stringHelper().fromUnderScoreCaseToCamelCase(fieldName) + "Enum";
    }

    public String looseEnumName() {
        return this.enumName;
    }

    public String build() {
        StringBuilder code = new StringBuilder();
        code
                .append("\t/**\n")
                .append("\t * Enum for Field `").append(fieldName).append("` \n")
                .append("\t */\n")
                .append("\tpublic enum ").append(looseEnumName()).append(" {\n");
        enumElements.forEach(enumValue -> {
            code.append("\t\t").append(enumValue).append(",\n");
        });
        code.append("\t}\n");

        return code.toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
