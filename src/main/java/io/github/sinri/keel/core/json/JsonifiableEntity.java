package io.github.sinri.keel.core.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.ClusterSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

/**
 * @since 1.14
 * @since 2.8 ClusterSerializable: Safe with EventBus Messaging.
 * @since 2.8 Shareable: allows you to put into a LocalMap.
 * @since 2.8 Iterable: you can run forEach with it.
 */
public interface JsonifiableEntity<E> extends UnmodifiableJsonifiableEntity, ClusterSerializable {

    @Nonnull
    E reloadDataFromJsonObject(@Nonnull JsonObject jsonObject);

    /**
     * @param <B> an implementation class of JsonifiableEntity, with constructor B() or B(JsonObject).
     * @since 2.7
     */
    default @Nullable <B extends JsonifiableEntity<?>> B readJsonifiableEntity(@Nonnull Class<B> bClass, String... args) {
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
    default void fromBuffer(@Nonnull Buffer buffer) {
        this.reloadDataFromJsonObject(new JsonObject(buffer));
    }

    /**
     * @since 2.8
     */
    default void writeToBuffer(@Nonnull Buffer buffer) {
        JsonObject jsonObject = this.toJsonObject();
        jsonObject.writeToBuffer(buffer);
    }

    /**
     * @since 2.8
     */
    default int readFromBuffer(int pos, @Nonnull Buffer buffer) {
        JsonObject jsonObject = new JsonObject();
        int i = jsonObject.readFromBuffer(pos, buffer);
        this.reloadDataFromJsonObject(jsonObject);
        return i;
    }


}
