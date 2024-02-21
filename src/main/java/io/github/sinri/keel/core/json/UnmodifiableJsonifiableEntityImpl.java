package io.github.sinri.keel.core.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * @since 3.1.10
 */
class UnmodifiableJsonifiableEntityImpl implements UnmodifiableJsonifiableEntity {
    private final @Nonnull JsonObject jsonObject;

    public UnmodifiableJsonifiableEntityImpl(@Nonnull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * @since 2.7
     * @since 2.8 If java.lang.ClassCastException occurred, return null instead.
     * @since 3.1.10 make it abstract.
     */
    @Override
    public <T> @Nullable T read(@Nonnull Function<JsonPointer, Class<T>> func) {
        try {
            JsonPointer jsonPointer = JsonPointer.create();
            Class<T> tClass = func.apply(jsonPointer);
            Object o = jsonPointer.queryJson(jsonObject);
            if (o == null) {
                return null;
            }
            return tClass.cast(o);
        } catch (ClassCastException castException) {
            return null;
        }
    }

    /**
     * @since 2.8
     * @since 3.1.10 make it abstract.
     */
    @Override
    public Buffer toBuffer() {
        return jsonObject.toBuffer();
    }

    /**
     * @since 3.0.0
     * @since 3.1.10 make it abstract.
     */
    @Nonnull
    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return jsonObject.iterator();
    }

    @Override
    public boolean isEmpty() {
        return jsonObject.isEmpty();
    }
}
