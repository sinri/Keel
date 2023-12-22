package io.github.sinri.keel.core;

import javax.annotation.Nullable;

/**
 * Store a value or not, and provide the ability to determine if the value is set, even if the value is null.
 *
 * @param <T> the type of value
 * @since 3.0.19
 */
@TechnicalPreview(since = "3.0.19")
public class ValueBox<T> {
    private T value;
    private boolean valueAlreadySet;

    public ValueBox() {
        this.clear();
    }

    public ValueBox(@Nullable T value) {
        this.setValue(value);
    }

    public ValueBox<T> clear() {
        this.value = null;
        this.valueAlreadySet = false;
        return this;
    }

    public boolean isValueAlreadySet() {
        return valueAlreadySet;
    }

    public T getValue() {
        if (isValueAlreadySet()) return value;
        else throw new IllegalStateException("Value is not set yet");
    }

    public ValueBox<T> setValue(@Nullable T value) {
        this.value = value;
        this.valueAlreadySet = true;
        return this;
    }

    public boolean isValueSetToNull() {
        return this.isValueAlreadySet() && this.getValue() == null;
    }
}
