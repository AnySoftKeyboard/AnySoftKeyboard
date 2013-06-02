package com.anysoftkeyboard.dictionaries;

import android.os.AsyncTask;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;

import java.lang.ref.WeakReference;

/**
 * A generic AsyncTask to load AnySoftKeyboard's dictionary object.
 * User: menny
 * Date: 3/19/13
 * Time: 11:52 AM
 */
public class DictionaryASyncLoader extends AsyncTask<Dictionary, Void, Dictionary> {
    private static final String TAG = "ASK_DictionaryASyncLoader";
    private final WeakReference<Listener> mListener;
    private Exception mException = null;

    private boolean mLoadingStarted = false;
    private final Object mLoadingStartedMonitor = new Object();

    public DictionaryASyncLoader(Listener listener) {
        mListener = new WeakReference<Listener>(listener);
    }

    @Override
    protected Dictionary doInBackground(Dictionary... dictionaries) {
        synchronized (mLoadingStartedMonitor) {
            mLoadingStarted = true;
            //notifying waiters
            mLoadingStartedMonitor.notifyAll();
        }
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

    /**
     * This method will help in the rare case the access to the dictionary is done before loading started (hence the storage is not ready)
     */
    public void waitTillLoadingStarted() {
        //I know that this is not very _engineering_ correct, but I know this:
        //this function will always be called for a thread that actually WAITS for the dictionary loading to start,
        // so, if the thread is going to wait, why not just yield now, and let the other thread just do it!
        Thread.yield();

        synchronized (mLoadingStartedMonitor) {
            if (mLoadingStarted == false) {
                try {
                    mLoadingStartedMonitor.wait();
                    waitTillLoadingStarted();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public static interface Listener {
        void onDictionaryLoadingDone(Dictionary dictionary);

        void onDictionaryLoadingFailed(Dictionary dictionary, Exception exception);
    }
}
