package io.github.sinri.keel.core.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.ClusterSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.14
 * @since 2.8 ClusterSerializable: Safe with EventBus Messaging.
 * @since 2.8 Shareable: allows you to put into a LocalMap.
 * @since 2.8 Iterable: you can run forEach with it.
 */
public interface JsonifiableEntity<E> extends UnmodifiableJsonifiableEntity, ClusterSerializable {

    @Nonnull
    E reloadDataFromJsonObject(JsonObject jsonObject);


    /**
     * @since 2.8
     */
    @Deprecated(since = "3.0.0")
    default @Nullable <T extends SimpleJsonifiableEntity> List<T> readEntityArray(Class<T> classOfEntity, String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<T> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(null);
            } else if (x instanceof JsonObject) {
                try {
                    T t = classOfEntity.getConstructor().newInstance();
                    t.reloadDataFromJsonObject((JsonObject) x);
                    list.add(t);
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("NOT JSON OBJECT");
            }
        });
        return list;
    }

    /**
     * @param <B> an implementation class of JsonifiableEntity, with constructor B() or B(JsonObject).
     * @since 2.7
     */
    default @Nullable <B extends JsonifiableEntity<?>> B readJsonifiableEntity(Class<B> bClass, String... args) {
        JsonObject jsonObject = readJsonObject(args);
        if (jsonObject == null) return null;
        try {
            var x = bClass.getConstructor().newInstance();
            x.reloadDataFromJsonObject(jsonObject);
            return x;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException ignored1) {
            try {
                return bClass.getConstructor(JsonObject.class).newInstance(jsonObject);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException ignored2) {
                return null;
            }
        }
    }


    /**
     * @since 2.8
     */
    default void fromBuffer(Buffer buffer) {
        this.reloadDataFromJsonObject(new JsonObject(buffer));
    }

    /**
     * @since 2.8
     */
    default void writeToBuffer(Buffer buffer) {
        JsonObject jsonObject = this.toJsonObject();
        jsonObject.writeToBuffer(buffer);
    }

    /**
     * @since 2.8
     */
    default int readFromBuffer(int pos, Buffer buffer) {
        JsonObject jsonObject = new JsonObject();
        int i = jsonObject.readFromBuffer(pos, buffer);
        this.reloadDataFromJsonObject(jsonObject);
        return i;
    }


}
