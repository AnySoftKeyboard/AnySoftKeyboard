package com.anysoftkeyboard.dictionaries;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;

import com.anysoftkeyboard.base.dictionaries.Dictionary;
import com.anysoftkeyboard.utils.Log;

import java.lang.ref.WeakReference;

/**
 * A generic AsyncTask to load AnySoftKeyboard's dictionary object.
 */
public final class DictionaryASyncLoader extends AsyncTask<Dictionary, Void, Dictionary> {
    private static final String TAG = "ASK_DictionaryASyncLoader";

    public static DictionaryASyncLoader executeLoaderParallel(@Nullable Listener listener, @NonNull Dictionary dictionary) {
        final DictionaryASyncLoader task = new DictionaryASyncLoader(listener);
        AsyncTaskCompat.executeParallel(task, dictionary);
        return task;
    }

    private final WeakReference<Listener> mListener;
    private Exception mException = null;

    private DictionaryASyncLoader(Listener listener) {
        mListener = new WeakReference<>(listener);
    }

    @Override
    protected Dictionary doInBackground(Dictionary... dictionaries) {
        Dictionary dictionary = dictionaries[0];
        if (!dictionary.isClosed()) {
            try {
                dictionary.loadDictionary();
            } catch (Exception e) {
                Log.w(TAG, "Failed to load dictionary!", e);
                mException = e;
            }
        }

        return dictionary;
    }

    @Override
    protected void onPostExecute(Dictionary dictionary) {
        super.onPostExecute(dictionary);
        if (!dictionary.isClosed()) {
            if (mException != null) {
                dictionary.close();
            }

            Listener listener = mListener.get();
            if (listener == null) return;
            if (mException == null) {
                listener.onDictionaryLoadingDone(dictionary);
            } else {
                listener.onDictionaryLoadingFailed(dictionary, mException);
            }
        }
    }

    public interface Listener {
        void onDictionaryLoadingDone(Dictionary dictionary);
        void onDictionaryLoadingFailed(Dictionary dictionary, Exception exception);
    }
}
