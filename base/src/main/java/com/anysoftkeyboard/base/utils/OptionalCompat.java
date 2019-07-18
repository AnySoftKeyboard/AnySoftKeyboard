package com.anysoftkeyboard.base.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class OptionalCompat<T> {
    public static <T> OptionalCompat<T> of(@Nullable T value) {
        return new OptionalCompat<>(value);
    }

    @Nullable private final T mValue;

    private OptionalCompat(T value) {
        mValue = value;
    }

    @Nullable
    public T get() {
        return mValue;
    }

    public boolean isPresent() {
        return mValue != null;
    }

    public boolean isEmpty() {
        return mValue == null;
    }

    @NonNull
    public T getOrElse(@NonNull T defaultValue) {
        if (mValue == null) return defaultValue;
        else return mValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OptionalCompat)) {
            return false;
        }

        OptionalCompat<?> other = (OptionalCompat<?>) obj;
        if (other.isEmpty() && isEmpty()) {
            return true;
        }

        if (other.mValue == mValue) {
            return true;
        }

        return (other.mValue != null && other.mValue.equals(mValue));
    }

    @Override
    public int hashCode() {
        return mValue == null ? 0 : mValue.hashCode() + 1;
    }
}
