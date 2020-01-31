package com.anysoftkeyboard.utils;

public class Triple<F, S, T> {
    public static <F, S, T> Triple<F, S, T> create(F f, S s, T t) {
        return new Triple<>(f, s, t);
    }

    private final F mFirst;
    private final S mSecond;
    private final T mThird;

    public Triple(F first, S second, T third) {
        mFirst = first;
        mSecond = second;
        mThird = third;
    }

    public F getFirst() {
        return mFirst;
    }

    public S getSecond() {
        return mSecond;
    }

    public T getThird() {
        return mThird;
    }
}
