package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * @since 2.7
 */
public class JsonStringScheme extends JsonValueScheme<String> {

    private Pattern pattern;
    private boolean allowEmpty = true;
    private boolean allowBlank = true;

    public Pattern getPattern() {
        return pattern;
    }

    public JsonStringScheme setPattern(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    public boolean isAllowBlank() {
        return allowBlank;
    }

    public JsonStringScheme setAllowBlank(boolean allowBlank) {
        this.allowBlank = allowBlank;
        return this;
    }

    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    public JsonStringScheme setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
        return this;
    }

    @Override
    public JsonElementSchemeType getJsonElementSchemeType() {
        return JsonElementSchemeType.JsonString;
    }

    @Override
    public @NotNull JsonObject toJsonObject() {
        return super.toJsonObject()
                .put("scheme_type", getJsonElementSchemeType())
                .put("pattern", pattern)
                .put("allow_blank", allowBlank)
                .put("allow_empty", allowEmpty);
    }

    @Override
    public @NotNull JsonElementScheme<String> reloadDataFromJsonObject(JsonObject jsonObject) {
        super.reloadDataFromJsonObject(jsonObject);

        this.allowBlank = jsonObject.getBoolean("allow_blank", true);
        this.allowEmpty = jsonObject.getBoolean("allow_empty", true);

        this.pattern = null;
        var regex = jsonObject.getString("pattern");
        if (regex != null) {
            pattern = Pattern.compile(regex);
        }

        return this;
    }

//    @Override
//    public void validate(Object object) throws JsonSchemeMismatchException {
//        if (object == null) {
//            if(! isNullable()){
//                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleNullableNotAllowed);
//            }
//        }
//        if (object instanceof String) {
//            if (((String) object).isEmpty() && !isAllowEmpty()) {
//                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//            }
//            if (((String) object).isBlank() && !isAllowBlank()) {
//                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//            }
//            if (this.pattern != null) {
//                if(! this.pattern.matcher((CharSequence) object).matches()){
//                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//                }
//            }
//        }else {
//            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueTypeNotExpected);
//        }
//    }

    @Override
    public void digest(String object) throws JsonSchemeMismatchException {
        if (object == null) {
            if (!isNullable()) {
                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleNullableNotAllowed);
            }
            return;
        }
//        if (object instanceof String) {
        if (object.isEmpty() && !isAllowEmpty()) {
            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
        }
        if (object.isBlank() && !isAllowBlank()) {
            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
        }
        if (this.pattern != null) {
            if (!this.pattern.matcher(object).matches()) {
                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
            }
        }
//        }else {
//            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueTypeNotExpected);
//        }
        this.digested = object;
    }

    @Override
    public String getDigested() {
        return this.digested;
    }
}
