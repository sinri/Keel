package io.github.sinri.keel.core.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * @since 3.1.10
 * @since 3.2.15 be public
 */
public class UnmodifiableJsonifiableEntityImpl implements UnmodifiableJsonifiableEntity {
    private final @NotNull JsonObject jsonObject;

    public UnmodifiableJsonifiableEntityImpl(@NotNull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * @since 2.7
     * @since 2.8 If java.lang.ClassCastException occurred, return null instead.
     * @since 3.1.10 make it abstract.
     */
    @Override
    public <T> @Nullable T read(@NotNull Function<JsonPointer, Class<T>> func) {
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


    @Override
    public @NotNull Iterator<Map.Entry<String, Object>> iterator() {
        return jsonObject.iterator();
    }

    @Override
    public boolean isEmpty() {
        return jsonObject.isEmpty();
    }
}
