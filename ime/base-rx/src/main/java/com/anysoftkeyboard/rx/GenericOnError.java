package com.anysoftkeyboard.rx;

import com.anysoftkeyboard.base.utils.Logger;
import io.reactivex.functions.Consumer;

public class GenericOnError<T extends Throwable> implements Consumer<T> {

    private final String mMessage;

    private GenericOnError(String message) {
        mMessage = message;
    }

    @Override
    public void accept(T throwable) throws Exception {
        Logger.w("GenericOnError", throwable, mMessage);
    }

    public static <T extends Throwable> GenericOnError<T> onError(String message) {
        return new GenericOnError<>(message);
    }
}
