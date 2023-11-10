package io.github.sinri.keel.helper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @since 2.6
 */
public class KeelJsonHelper {
    private static final KeelJsonHelper instance = new KeelJsonHelper();

    private KeelJsonHelper() {

    }

    static KeelJsonHelper getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        KeelJsonHelper keelJsonHelper = new KeelJsonHelper();
        var x1 = keelJsonHelper.renderJsonToStringBlock("x1", new JsonObject()
                .put("a", "A")
                .put("b", new JsonObject()
                        .put("c", "d")
                        .put("e", new JsonArray()
                                .add("f")
                                .add("g")
                                .add(new JsonObject()
                                        .put("h", "i")
                                        .put("j", "k")
                                )
                                .add(new JsonArray()
                                        .add("l")
                                        .add("m")))
                )
                .put("n", new JsonArray()
                        .add("o")
                        .add(null))
        );
        System.out.println(x1);
    }

    public JsonObject writeIntoJsonObject(@Nonnull JsonObject jsonObject, @Nonnull String key, @Nullable Object value) {
        jsonObject.put(key, value);
        return jsonObject;
    }

    public JsonArray writeIntoJsonArray(@Nonnull JsonArray jsonArray, int index, @Nullable Object value) {
        if (index >= 0) {
            if (index >= jsonArray.size()) {
                for (var i = jsonArray.size(); i <= index; i++) {
                    jsonArray.add(null);
                }
            }
            jsonArray.set(index, value);
        } else {
            jsonArray.add(value);
        }
        return jsonArray;
    }

    /**
     * @throws RuntimeException if not writable
     */
    @Nonnull
    public JsonObject writeIntoJsonObject(@Nonnull JsonObject jsonObject, @Nonnull List<Object> keychain, @Nullable Object value) {
        Objects.requireNonNull(jsonObject);
        if (keychain.isEmpty()) {
            throw new RuntimeException();
        }
        if (keychain.size() == 1) {
            writeIntoJsonObject(jsonObject, String.valueOf(keychain.get(0)), value);
            return jsonObject;
        }

        Object key = keychain.get(0);
        Object nextKey = keychain.get(1);
        List<Object> nestedKeychain = keychain.subList(1, keychain.size());

        if (jsonObject.containsKey(String.valueOf(key))) {
            Object nextObject = jsonObject.getValue(String.valueOf(key));
            if (nextObject instanceof JsonObject) {
                writeIntoJsonObject((JsonObject) nextObject, nestedKeychain, value);
            } else if (nextObject instanceof JsonArray) {
                writeIntoJsonArray((JsonArray) nextObject, nestedKeychain, value);
            } else {
                throw new RuntimeException();
            }
        } else {
            if (nextKey instanceof Long || nextKey instanceof Integer || nextKey instanceof Short) {
                JsonArray array = new JsonArray();
                jsonObject.put(String.valueOf(key), array);
                writeIntoJsonArray(array, nestedKeychain, value);
            } else {
                JsonObject object = new JsonObject();
                jsonObject.put(String.valueOf(key), object);
                writeIntoJsonObject(object, nestedKeychain, value);
            }
        }

        return jsonObject;
    }

    @Nonnull
    public JsonArray writeIntoJsonArray(@Nonnull JsonArray jsonArray, @Nonnull List<Object> keychain, @Nullable Object value) {
        Objects.requireNonNull(keychain);
        if (keychain.isEmpty()) {
            throw new RuntimeException();
        }
        if (keychain.size() == 1) {
            Object key = keychain.get(0);
            if (key instanceof Long || key instanceof Integer || key instanceof Short) {
                int index = ((Number) key).intValue();
                writeIntoJsonArray(jsonArray, index, value);
                return jsonArray;
            } else {
                throw new RuntimeException();
            }
        }

        Object key = keychain.get(0);
        Object nextKey = keychain.get(1);
        List<Object> nestedKeychain = keychain.subList(1, keychain.size());

        if (key instanceof Long || key instanceof Integer || key instanceof Short) {
            int index = ((Number) key).intValue();
            if (jsonArray.size() > index && index >= 0) {
                // contains
                Object existed = jsonArray.getValue(index);
                if (existed instanceof JsonArray) {
                    if (nextKey instanceof Long || nextKey instanceof Integer || nextKey instanceof Short) {
                        writeIntoJsonArray((JsonArray) existed, nestedKeychain, value);
                    } else {
                        throw new RuntimeException();
                    }
                } else if (existed instanceof JsonObject) {
                    writeIntoJsonObject((JsonObject) existed, nestedKeychain, value);
                } else {
                    throw new RuntimeException();
                }
            } else {
                // not contained
                if (nextKey instanceof Long || nextKey instanceof Integer || nextKey instanceof Short) {
                    writeIntoJsonArray(jsonArray, index, writeIntoJsonArray(new JsonArray(), nestedKeychain, value));
                } else {
                    writeIntoJsonArray(jsonArray, index, writeIntoJsonObject(new JsonObject(), nestedKeychain, value));
                }
            }
        } else {
            throw new RuntimeException();
        }

        return jsonArray;
    }

    @Nullable
    public Object readFromJsonObject(@Nonnull JsonObject jsonObject, @Nonnull String key) {
        return jsonObject.getValue(key);
    }

    @Nullable
    public Object readFromJsonArray(@Nonnull JsonArray jsonArray, int index) {
        return jsonArray.getValue(index);
    }

    @Nullable
    public Object readFromJsonObject(@Nonnull JsonObject jsonObject, @Nonnull List<Object> keychain) {
        Objects.requireNonNull(keychain);
        if (keychain.isEmpty()) {
            throw new RuntimeException();
        }
        var key = keychain.get(0);
        Object x = readFromJsonObject(jsonObject, String.valueOf(key));
        if (keychain.size() == 1) {
            return x;
        }
        List<Object> nextKeychain = keychain.subList(1, keychain.size());
        if (x instanceof JsonObject) {
            return readFromJsonObject((JsonObject) x, nextKeychain);
        } else if (x instanceof JsonArray) {
            return readFromJsonArray((JsonArray) x, nextKeychain);
        }
        throw new RuntimeException();
    }

    @Nullable
    public Object readFromJsonArray(@Nonnull JsonArray jsonArray, @Nonnull List<Object> keychain) {
        Objects.requireNonNull(keychain);
        if (keychain.isEmpty()) {
            throw new RuntimeException();
        }
        var key = keychain.get(0);
        if (key instanceof Long || key instanceof Integer || key instanceof Short) {
            Object x = readFromJsonArray(jsonArray, ((Number) key).intValue());
            if (keychain.size() == 1) {
                return x;
            }
            List<Object> nextKeychain = keychain.subList(1, keychain.size());
            if (x instanceof JsonObject) {
                return readFromJsonObject((JsonObject) x, nextKeychain);
            } else if (x instanceof JsonArray) {
                return readFromJsonArray((JsonArray) x, nextKeychain);
            }
            throw new RuntimeException();
        }
        throw new RuntimeException();
    }

    /**
     * @since 2.4
     */
    @Nonnull
    private JsonArray getSortedJsonArray(@Nonnull JsonArray array) {
        List<Object> list = new ArrayList<>();
        array.forEach(list::add);
        list.sort(Comparator.comparing(Object::toString));
        return new JsonArray(list);
    }

    /**
     * @since 2.4
     */
    @Nonnull
    public String getJsonForArrayWhoseItemsSorted(@Nonnull JsonArray array) {
        return getSortedJsonArray(array).toString();
    }

    /**
     * @since 2.4
     */
    @Nonnull
    private JsonObject getSortedJsonObject(@Nonnull JsonObject object) {
        JsonObject result = new JsonObject();
        List<String> keyList = new ArrayList<>(object.getMap().keySet());
        keyList.sort(Comparator.naturalOrder());
        keyList.forEach(key -> {
            Object value = object.getValue(key);
            if (value instanceof JsonObject) {
                result.put(key, getSortedJsonObject((JsonObject) value));
            } else if (value instanceof JsonArray) {
                result.put(key, getSortedJsonArray((JsonArray) value));
            } else {
                result.put(key, value);
            }
        });
        return result;
    }

    /**
     * @since 2.4
     */
    @Nonnull
    public String getJsonForObjectWhoseItemKeysSorted(@Nonnull JsonObject object) {
        return getSortedJsonObject(object).toString();
    }

    /**
     * @since 3.0.0
     */
    @Nullable
    public JsonObject renderThrowableChain(@Nullable Throwable throwable) {
        return renderThrowableChain(throwable, Set.of());
    }

    /**
     * @since 2.9
     */
    @Nullable
    public JsonObject renderThrowableChain(@Nullable Throwable throwable, @Nonnull Set<String> ignorableStackPackageSet) {
        if (throwable == null) return null;

        Throwable cause = throwable.getCause();
        JsonObject x = new JsonObject()
                .put("class", throwable.getClass().getName())
                .put("message", throwable.getMessage())
                .put("stack", filterStackTraceToJsonArray(throwable.getStackTrace(), ignorableStackPackageSet))
                .put("cause", null);

        JsonObject upper = x;
        while (cause != null) {
            JsonObject current = new JsonObject()
                    .put("class", cause.getClass().getName())
                    .put("message", cause.getMessage())
                    .put("stack", filterStackTraceToJsonArray(cause.getStackTrace(), ignorableStackPackageSet))
                    .put("cause", null);
            upper.put("cause", current);
            upper = current;

            cause = cause.getCause();
        }
        return x;
    }

    public void filterStackTrace(
            @Nullable StackTraceElement[] stackTrace,
            @Nonnull Set<String> ignorableStackPackageSet,
            @Nonnull BiConsumer<String, Integer> ignoredStackTraceItemsConsumer,
            @Nonnull Consumer<StackTraceElement> stackTraceItemConsumer
    ) {
        if (stackTrace != null) {
            String ignoringClassPackage = null;
            int ignoringCount = 0;
            for (StackTraceElement stackTranceItem : stackTrace) {
                String className = stackTranceItem.getClassName();
                String matchedClassPackage = null;
                for (var cp : ignorableStackPackageSet) {
                    if (className.startsWith(cp)) {
                        matchedClassPackage = cp;
                        break;
                    }
                }
                if (matchedClassPackage == null) {
                    if (ignoringCount > 0) {
                        ignoredStackTraceItemsConsumer.accept(ignoringClassPackage, ignoringCount);
                        ignoringClassPackage = null;
                        ignoringCount = 0;
                    }

                    stackTraceItemConsumer.accept(stackTranceItem);
                } else {
                    if (ignoringCount > 0) {
                        if (ignoringClassPackage.equals(matchedClassPackage)) {
                            ignoringCount += 1;
                        } else {
                            ignoredStackTraceItemsConsumer.accept(ignoringClassPackage, ignoringCount);
                            ignoringClassPackage = matchedClassPackage;
                            ignoringCount = 1;
                        }
                    } else {
                        ignoringClassPackage = matchedClassPackage;
                        ignoringCount = 1;
                    }
                }
            }
            if (ignoringCount > 0) {
                ignoredStackTraceItemsConsumer.accept(ignoringClassPackage, ignoringCount);
            }
        }
    }

    /**
     * @since 2.9 original name: buildStackChainText
     * @since 3.0.0 become private and renamed to filterStackTraceToJsonArray
     */
    @Nonnull
    private JsonArray filterStackTraceToJsonArray(@Nullable StackTraceElement[] stackTrace, @Nonnull Set<String> ignorableStackPackageSet) {
        JsonArray array = new JsonArray();

        filterStackTrace(
                stackTrace,
                ignorableStackPackageSet,
                (ignoringClassPackage, ignoringCount) -> array.add(new JsonObject()
                        .put("type", "ignored")
                        .put("package", ignoringClassPackage)
                        .put("count", ignoringCount)
                ),
                stackTranceItem -> array.add(new JsonObject()
                        .put("type", "call")
                        .put("class", stackTranceItem.getClassName())
                        .put("method", stackTranceItem.getMethodName())
                        .put("file", stackTranceItem.getFileName())
                        .put("line", stackTranceItem.getLineNumber())
                )
        );

        return array;
    }

    /**
     * @since 3.0.0
     */
    @Nonnull
    public String renderJsonToStringBlock(@Nullable String name, @Nullable Object object) {
        if (object == null) {
            return "null";
        }
        return renderJsonItem(name, object, 0, null);
    }

    /**
     * @param key    Key of entry amongst the entries, or the index amongst the array.
     * @param object Value of entry amongst the entries, or the item amongst the array.
     * @return rendered string block ended with NEW_LINE.
     */
    @Nonnull
    private String renderJsonItem(@Nullable String key, @Nullable Object object, int indentation, @Nullable String typeMark) {
        StringBuilder subBlock = new StringBuilder();
        if (indentation > 1) {
            subBlock.append(" ".repeat(indentation - 2));
            subBlock.append(typeMark).append(" ");
        } else {
            subBlock.append(" ".repeat(Math.max(0, indentation)));
        }

        if (key != null) {
            subBlock.append(key).append(": ");
        }
        if (object instanceof JsonObject) {
            subBlock.append("\n");
            ((JsonObject) object).forEach(entry -> {
                subBlock.append(
                        renderJsonItem(entry.getKey(), entry.getValue(), indentation + 2, "+")
                );
            });
        } else if (object instanceof JsonArray) {
            subBlock.append("\n");
            for (int i = 0; i < ((JsonArray) object).size(); i++) {
                subBlock.append(
                        renderJsonItem(i + "", ((JsonArray) object).getValue(i), indentation + 2, "-")
                );
            }
        } else {
            subBlock.append(object).append("\n");
        }
        return subBlock.toString();
    }
}
