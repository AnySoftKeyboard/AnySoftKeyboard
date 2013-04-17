package com.anysoftkeyboard.dictionaries;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * Created with IntelliJ IDEA.
 * User: menny
 * Date: 3/19/13
 * Time: 11:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class DictionaryASyncLoader extends AsyncTask<Dictionary, Void, Dictionary> {
    private final WeakReference<Listener> mListener;
    private Exception mException = null;

    public DictionaryASyncLoader(Listener listener) {
        mListener = new WeakReference<Listener>(listener);
    }

    @Override
    protected Dictionary doInBackground(Dictionary... dictionaries) {
        Dictionary dictionary = dictionaries[0];
        if (!dictionary.isClosed()) {
            dictionary.loadDictionary();
        }

        return dictionary;
    }

    @Override
    protected void onPostExecute(Dictionary dictionary) {
        super.onPostExecute(dictionary);
        if (!dictionary.isClosed()) {
            Listener listener = mListener.get();
            if (listener == null) return;
            if (mException == null) {
                listener.onDictionaryLoadingDone(dictionary);
            } else {
                listener.onDictionaryLoadingFailed(dictionary, mException);
            }
        }
    }

    public static interface Listener {
        void onDictionaryLoadingDone(Dictionary dictionary);

        void onDictionaryLoadingFailed(Dictionary dictionary, Exception exception);
    }
}
