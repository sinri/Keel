package io.github.sinri.keel.core.json;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.0
 * Experimental
 */
public class JsonObjectValidator {
    protected final List<JsonObjectItemValidator> itemValidators;

    public JsonObjectValidator() {
        this.itemValidators = new ArrayList<>();
    }

    public static void main(String[] args) {
        JsonObject j = new JsonObject()
                .putNull("a")
                .put("b", "B")
                .put("c", 3)
                .put("d", false)
                .put("e", new JsonObject()
                        .put("z", "1")
                )
                .put("f", "333");
        JsonObjectValidator jsonObjectValidator = new JsonObjectValidator()
                .addItemValidator(
                        new JsonObjectItemValidator(
                                JsonObjectItemValidator.ExistenceType.REQUIRED,
                                "a",
                                v -> true
                        )
                )
                .addItemValidator(
                        new JsonObjectItemValidator(
                                JsonObjectItemValidator.ExistenceType.REQUIRED,
                                "b",
                                JsonObjectItemValidator::validateAsNotEmptyString
                        )
                )
                .addItemValidator(
                        new JsonObjectItemValidator(
                                JsonObjectItemValidator.ExistenceType.REQUIRED,
                                "c",
                                v -> ((Integer) v) > 2
                        )
                )
                .addItemValidator(
                        new JsonObjectItemValidator(
                                JsonObjectItemValidator.ExistenceType.REQUIRED,
                                "d",
                                JsonObjectItemValidator::validateAsBoolean
                        )
                )
                .addItemValidator(
                        new JsonObjectItemValidator(
                                JsonObjectItemValidator.ExistenceType.REQUIRED,
                                "e",
                                v -> {
                                    if (v instanceof JsonObject) {
                                        return new JsonObjectValidator()
                                                .addItemValidator(new JsonObjectItemValidator(
                                                        JsonObjectItemValidator.ExistenceType.REQUIRED,
                                                        "z",
                                                        JsonObjectItemValidator::validateAsNotEmptyString
                                                ))
                                                .validate((JsonObject) v);
                                    } else {
                                        return false;
                                    }
                                }
                        )
                )
                .addItemValidator(
                        new JsonObjectItemValidator(
                                JsonObjectItemValidator.ExistenceType.FORBIDDEN,
                                "f",
                                null
                        )
                );
        boolean validated = jsonObjectValidator.validate(j);
        System.out.println("validated: " + validated);
    }

    public JsonObjectValidator addItemValidator(JsonObjectItemValidator itemValidator) {
        this.itemValidators.add(itemValidator);
        return this;
    }

    public boolean validate(JsonObject targetJsonObject) {
        for (var itemValidator : itemValidators) {
            JsonObjectItemValidator.ExistenceType existenceType = itemValidator.getExistenceType();
            // existence
            if (existenceType == JsonObjectItemValidator.ExistenceType.REQUIRED) {
                if (!targetJsonObject.containsKey(itemValidator.getKey())) {
                    return false;
                }
            }
            if (existenceType == JsonObjectItemValidator.ExistenceType.OPTIONAL) {
                if (!targetJsonObject.containsKey(itemValidator.getKey())) {
                    continue;
                }
            }
            if (existenceType == JsonObjectItemValidator.ExistenceType.FORBIDDEN) {
                if (targetJsonObject.containsKey(itemValidator.getKey())) {
                    return false;
                } else {
                    continue;
                }
            }
            // value
            Object value = targetJsonObject.getValue(itemValidator.getKey());
            boolean validateValue = itemValidator.validateValue(value);
            if (!validateValue) return false;
        }
        return true;
    }
}
