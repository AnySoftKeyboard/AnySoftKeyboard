package com.anysoftkeyboard.dictionaries;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.rx.RxSchedulers;

import io.reactivex.Observable;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.disposables.Disposable;

/**
 * A generic RX chain to load AnySoftKeyboard's dictionary object.
 */
public final class DictionaryBackgroundLoader {

    @CheckReturnValue
    public static Disposable loadDictionaryInBackground(@NonNull Dictionary dictionary) {
        return loadDictionaryInBackground(NO_OP_LISTENER, dictionary);
    }

    @CheckReturnValue
    public static Disposable loadDictionaryInBackground(@NonNull Listener listener, @NonNull Dictionary dictionary) {
        return Observable.<Pair<Listener, Dictionary>>create(emitter -> emitter.onNext(Pair.create(listener, dictionary)))
                .subscribeOn(RxSchedulers.background())
                .map(pair -> {
                    pair.second.loadDictionary();
                    return pair;
                })
                .doFinally(dictionary::close)
                .observeOn(RxSchedulers.mainThread())
                .unsubscribeOn(RxSchedulers.background())
                .subscribe(pair -> pair.first.onDictionaryLoadingDone(pair.second),
                        throwable -> listener.onDictionaryLoadingFailed(dictionary, throwable));
    }

    public interface Listener {
        void onDictionaryLoadingDone(Dictionary dictionary);

        void onDictionaryLoadingFailed(Dictionary dictionary, Throwable exception);
    }

    private static final Listener NO_OP_LISTENER = new Listener() {
        @Override
        public void onDictionaryLoadingDone(Dictionary dictionary) {
            Logger.d("DictionaryBackgroundLoader", "onDictionaryLoadingDone for %s", dictionary);
        }

        @Override
        public void onDictionaryLoadingFailed(Dictionary dictionary, Throwable exception) {
            Logger.e("DictionaryBackgroundLoader", exception, "onDictionaryLoadingFailed for %s with error %s", dictionary, exception.getMessage());
        }
    };
}
