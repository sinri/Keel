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
    /**
     * @since 3.1.0
     * When expire is equal or less than zero, never expire;
     * Or as the milliseconds to reserve the value.
     */
    private long expire = 0;

    public ValueBox() {
        this.clear();
    }

    public ValueBox(@Nullable T value) {
        this(value, 0);
    }

    public ValueBox(@Nullable T value, long lifetime) {
        this.setValue(value, lifetime);
    }

    public ValueBox<T> clear() {
        this.value = null;
        this.valueAlreadySet = false;
        this.expire = 0;
        return this;
    }

    /**
     * If the value is already set, and, not expired if lifetime declared.
     * When checked, if the value set but expired, it would be cleaned.
     */
    public boolean isValueAlreadySet() {
        if (!valueAlreadySet) return false;
        if (expire <= 0) {
            return true;
        }
        if (expire > System.currentTimeMillis()) {
            return true;
        }
        this.clear();
        return false;
    }

    public T getValue() {
        if (isValueAlreadySet()) return value;
        else throw new IllegalStateException("Value is not set yet");
    }

    /**
     * @param fallbackForInvalid it would be return when the value is not set yet.
     * @since 3.1.0
     */
    public T getValueOrElse(T fallbackForInvalid) {
        if (isValueAlreadySet()) return value;
        else return fallbackForInvalid;
    }

    public ValueBox<T> setValue(@Nullable T value, long lifetime) {
        this.value = value;
        this.valueAlreadySet = true;
        if (lifetime > 0) {
            this.expire = System.currentTimeMillis() + lifetime;
        } else {
            this.expire = 0;
        }
        return this;
    }

    /**
     * Set value without expiration.
     */
    public ValueBox<T> setValue(@Nullable T value) {
        return this.setValue(value, 0);
    }

    public boolean isValueSetToNull() {
        return this.isValueAlreadySet() && this.getValue() == null;
    }
}
