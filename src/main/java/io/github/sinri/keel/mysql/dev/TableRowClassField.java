package io.github.sinri.keel.mysql.dev;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.0.15
 * @since 3.0.18 Finished Technical Preview.
 * @since 3.1.0 Add support for AES encryption.
 * @since 3.1.7 Add deprecated field annotation.
 */
class TableRowClassField {
    private static final Pattern patternForLooseEnum;
    private static final Pattern patternForStrictEnum;
    private static final Pattern patternForAESEnvelope;

    static {
        patternForLooseEnum = Pattern.compile("Enum\\{([A-Za-z0-9_, ]+)}");
        patternForStrictEnum = Pattern.compile("Enum<([A-Za-z0-9_.]+)>");
        patternForAESEnvelope = Pattern.compile("AES<([A-Za-z0-9_.]+)>");
    }

    private final String field;
    private final String type;
    private final String comment;
    private final @Nullable String strictEnumPackage;
    private String returnType;
    private String readMethod;
    private @Nullable TableRowClassFieldLooseEnum looseEnum;
    private @Nullable TableRowClassFieldStrictEnum strictEnum;

    private final @Nullable String aesEnvelopePackage;
    private @Nullable TableRowClassFieldAesEncryption aesEncryption;

    /**
     * @since 3.1.7
     */
    private boolean fieldDeprecated = false;
    private String actualComment;

    public TableRowClassField(
            @Nonnull String field,
            @Nonnull String type,
            @Nullable String comment,
            @Nullable String strictEnumPackage,
            @Nullable String aesEnvelopePackage
    ) {
        this.field = field;
        this.type = type;
        this.comment = comment;
        this.strictEnumPackage = strictEnumPackage;
        this.aesEnvelopePackage = aesEnvelopePackage;

        parseType();
        parseComment();
    }

    protected void parseType() {
        returnType = "Object";
        readMethod = "readValue";

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
    }

    protected void parseComment() {
        if (type.contains("char") && comment != null) {
            // supportLooseEnum
            Matcher matcherForLoose = patternForLooseEnum.matcher(comment);
            if (matcherForLoose.find()) {
                String enumValuesString = matcherForLoose.group(1);
                String[] enumValueArray = enumValuesString.split("[, ]+");
                if (enumValueArray.length > 0) {
                    looseEnum = new TableRowClassFieldLooseEnum(field, List.of(enumValueArray));
                }
            }
            // supportStrictEnum
            Matcher matcherForStrict = patternForStrictEnum.matcher(comment);
            if (matcherForStrict.find()) {
                String enumClassPathTail = matcherForStrict.group(1);
                strictEnum = new TableRowClassFieldStrictEnum(field, strictEnumPackage, enumClassPathTail);
            }

            // AES Envelope
            Matcher matcherForAESEnvelope = patternForAESEnvelope.matcher(comment);
            if (matcherForAESEnvelope.find()) {
                String aes = matcherForAESEnvelope.group(1);
                aesEncryption = new TableRowClassFieldAesEncryption(aes, Objects.requireNonNull(this.aesEnvelopePackage));
            }
        }

        if (comment != null) {
            String[] split = comment.split("@[Dd]eprecated", 2);
            if (split.length > 1) {
                // this table is deprecated
                this.fieldDeprecated = true;
                actualComment = KeelHelpers.stringHelper().escapeForHttpEntity(split[1]);
            } else {
                actualComment = KeelHelpers.stringHelper().escapeForHttpEntity(comment);
            }
        } else {
            actualComment = "";
        }
    }

    public String build() {
        String getter = "get" + KeelHelpers.stringHelper().fromUnderScoreCaseToCamelCase(field);

        StringBuilder code = new StringBuilder();
        if (looseEnum != null) {
            code.append(looseEnum.build()).append("\n");
            code.append("\t/*\n")
                    .append("\t * ").append(actualComment).append("\n\t * \n")
                    .append("\t * Loose Enum of Field `").append(field).append("` of type `").append(type).append("`.\n")
                    .append("\t */\n");
            if (fieldDeprecated) {
                code.append("\t@Deprecated\n");
            }
            code
                    .append("\tpublic ").append(looseEnum.looseEnumName()).append(" ").append(getter).append("() {\n")
                    .append("\t\treturn ").append(looseEnum.looseEnumName()).append(".valueOf(\n")
                    .append("\t\t\t").append(readMethod).append("(\"").append(field).append("\")\n")
                    .append("\t\t);\n")
                    .append("\t}\n");
        } else if (strictEnum != null) {
            code.append("\t/*\n")
                    .append("\t * ").append(actualComment).append("\n\t * \n")
                    .append("\t * Strict Enum of Field `").append(field).append("` of type `").append(type).append("`.\n")
                    .append("\t */\n");
            if (fieldDeprecated) {
                code.append("\t@Deprecated\n");
            }
            code
                    .append("\tpublic ").append(strictEnum.fullEnumRef()).append(" ").append(getter).append("() {\n")
                    .append("\t\treturn ").append(strictEnum.fullEnumRef()).append(".valueOf(\n")
                    .append("\t\t\t").append(readMethod).append("(\"").append(field).append("\")\n")
                    .append("\t\t);\n")
                    .append("\t}\n");
        } else {
            code.append("\t/*\n");
            if (comment != null) {
                code.append("\t * ").append(actualComment).append("\n\t * \n");
            }
            code.append("\t * Field `").append(field).append("` of type `").append(type).append("`.\n")
                    .append("\t */\n");
            if (fieldDeprecated) {
                code.append("\t@Deprecated\n");
            }
            code.append("\tpublic ").append(returnType).append(" ").append(getter).append("() {\n")
                    .append("\t\treturn ").append(readMethod).append("(\"").append(field).append("\");\n")
                    .append("\t}\n");
        }

        if (aesEncryption != null) {
            code.append("\t/*\n")
                    .append("\t * AES DECRYPTED VALUE.\n");
            if (comment != null) {
                code.append("\t * ").append(actualComment).append("\n\t * \n");
            }
            code.append("\t */\n")
                    .append("\t@Nullable\n")
                    .append("\tpublic ").append("String").append(" ").append(getter).append("Decrypted() {\n")
                    .append("\t\treturn ").append(aesEncryption.buildCallClassMethodCode(readMethod + "(\"" + field + "\")")).append("\n")
                    .append("\t}\n");
        }

        return code.toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
