package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonObject;

import java.util.regex.Pattern;

/**
 * @since 2.7
 */
public class JsonStringScheme extends JsonValueScheme {

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
    public JsonObject toJsonObject() {
        return super.toJsonObject()
                .put("scheme_type", getJsonElementSchemeType())
                .put("pattern", pattern)
                .put("allow_blank", allowBlank)
                .put("allow_empty", allowEmpty);
    }

    @Override
    public JsonElementScheme reloadDataFromJsonObject(JsonObject jsonObject) {
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

    @Override
    public boolean validate(Object object) {
        if (object == null) {
            return isNullable();
        }
        if (object instanceof String) {
            if (((String) object).isEmpty() && !isAllowEmpty()) {
                return false;
            }
            if (((String) object).isBlank() && !isAllowBlank()) {
                return false;
            }
            if (this.pattern != null) {
                return this.pattern.matcher((CharSequence) object).matches();
            }
            return true;
        }
        return false;
    }
}
