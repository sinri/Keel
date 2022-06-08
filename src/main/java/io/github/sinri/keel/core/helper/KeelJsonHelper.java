package io.github.sinri.keel.core.helper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
/**
 * @since 2.6
 */
public class KeelJsonHelper {
    private static final KeelJsonHelper instance = new KeelJsonHelper();

    private KeelJsonHelper() {

    }

    public static KeelJsonHelper getInstance() {
        return instance;
    }

    public JsonObject writeIntoJsonObject(JsonObject jsonObject, String key, Object value) {
        jsonObject.put(String.valueOf(key), value);
        return jsonObject;
    }

    public JsonArray writeIntoJsonArray(JsonArray jsonArray, int index, Object value) {
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

    public JsonObject writeIntoJsonObject(JsonObject jsonObject, List<Object> keychain, Object value) {
        if (keychain == null || keychain.size() <= 0) {
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

    public JsonArray writeIntoJsonArray(JsonArray jsonArray, List<Object> keychain, Object value) {
        if (keychain == null || keychain.size() <= 0) {
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

    public Object readFromJsonObject(JsonObject jsonObject, String key) {
        return jsonObject.getValue(key);
    }

    public Object readFromJsonArray(JsonArray jsonArray, int index) {
        return jsonArray.getValue(index);
    }

    public Object readFromJsonObject(JsonObject jsonObject, List<Object> keychain) {
        if (keychain == null || keychain.isEmpty()) {
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

    public Object readFromJsonArray(JsonArray jsonArray, List<Object> keychain) {
        if (keychain == null || keychain.isEmpty()) {
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
    private JsonArray getSortedJsonArray(JsonArray array) {
        List<Object> list = new ArrayList<>();
        array.forEach(list::add);
        list.sort(Comparator.comparing(Object::toString));
        return new JsonArray(list);
    }

    /**
     * @since 2.4
     */
    public String getJsonForArrayWhoseItemsSorted(JsonArray array) {
        return getSortedJsonArray(array).toString();
    }

    /**
     * @since 2.4
     */
    private JsonObject getSortedJsonObject(JsonObject object) {
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
    public String getJsonForObjectWhoseItemKeysSorted(JsonObject object) {
        return getSortedJsonObject(object).toString();
    }
}
