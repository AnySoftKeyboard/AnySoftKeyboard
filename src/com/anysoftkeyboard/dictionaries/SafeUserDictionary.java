package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import android.os.AsyncTask;

import com.anysoftkeyboard.WordComposer;
import com.anysoftkeyboard.utils.Log;

public class SafeUserDictionary extends EditableDictionary {

	private static final String TAG = "ASK_SUD";
	private UserDictionaryBase mActualDictionary;

	private final Context mContext;

	private final Object mUpdatingLock = new Object();
	private boolean mUpdatingDictionary = false;
	private boolean mInitialLoaded = false;
	private final String mLocale;

	public SafeUserDictionary(Context context, String locale) {
		super("SafeUserDictionary");
		mLocale = locale;
		mContext = context;
	}

	@Override
	public synchronized void getWords(WordComposer composer,
			WordCallback callback) {
		if (mActualDictionary != null)
			mActualDictionary.getWords(composer, callback);
	}

	@Override
	public synchronized boolean isValidWord(CharSequence word) {
		if (mActualDictionary != null)
			return mActualDictionary.isValidWord(word);
		else
			return false;
	}

	@Override
	public synchronized void close() {
		if (mActualDictionary != null)
			mActualDictionary.close();
	}

	private class LoadDictionaryTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... v) {
			loadDictionarySync();
			return null;
		}
	}

	public synchronized void loadDictionarySync() {
		synchronized (mUpdatingLock) {
			mUpdatingDictionary = true;

			loadDictionaryAsync();
			mInitialLoaded = true;
			mUpdatingDictionary = false;
		}
	}

	private void loadDictionaryAsync() {
		AndroidUserDictionary androidBuiltIn = null;
		try {
			androidBuiltIn = new AndroidUserDictionary(mContext, mLocale);
			androidBuiltIn.loadDictionary();
			mActualDictionary = androidBuiltIn;
		} catch (Exception e) {
			Log.w(TAG,
					"Failed to load Android's built-in user dictionary. No matter, I'll use a fallback.");
			if (androidBuiltIn != null) {
				try {
					androidBuiltIn.close();
				} catch (Exception buildInCloseException) {
					// it's an half-baked object, no need to worry about it
					buildInCloseException.printStackTrace();
					Log.w(TAG,
							"Failed to close the build-in user dictionary properly, but it should be fine.");
				}
			}
			FallbackUserDictionary fallback = new FallbackUserDictionary(
					mContext, mLocale);
			fallback.loadDictionary();

			mActualDictionary = fallback;
		}
	}

	public void loadDictionary() {
		synchronized (mUpdatingLock) {
			if (!mUpdatingDictionary && !mInitialLoaded) {
				mUpdatingDictionary = true;
				new LoadDictionaryTask().execute();
			}
		}
	}

	@Override
	public synchronized boolean addWord(String word, int frequency) {
		if (mActualDictionary != null)
			return mActualDictionary.addWord(word, frequency);
		else
			return false;
	}

	@Override
	public synchronized WordsCursor getWordsCursor() {
		if (mActualDictionary != null)
			return mActualDictionary.getWordsCursor();
		
		return null;
	}

	@Override
	public synchronized void deleteWord(String word) {
		if (mActualDictionary != null)
			mActualDictionary.deleteWord(word);
	}

}
