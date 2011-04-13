package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.WordComposer;

import android.os.AsyncTask;
import android.util.Log;

public class SafeUserDictionary extends AddableDictionary {

	private static final String TAG = "ASK_SUD";
	private final AnyKeyboardContextProvider mAnyContext;
	private UserDictionaryBase mActualDictionary;

	private final Object mUpdatingLock = new Object();
	private boolean mUpdatingDictionary = false;
	private final String mLocale;
	
	protected SafeUserDictionary( AnyKeyboardContextProvider context, String locale) {
		super("SafeUserDictionary");
		mLocale = locale;
		mAnyContext = context;
	}

	@Override
	public void getWords(WordComposer composer, WordCallback callback) {
		if (mActualDictionary != null)
			mActualDictionary.getWords(composer, callback);
	}

	@Override
	public boolean isValidWord(CharSequence word) {
		if (mActualDictionary != null)
			return mActualDictionary.isValidWord(word);
		else
			return false;
	}

	@Override
	public void close() {
		if (mActualDictionary != null)
			mActualDictionary.close();
	}

	private class LoadDictionaryTask extends AsyncTask<Void, Void, Void> {
        
		@Override
        protected Void doInBackground(Void... v) {
            loadDictionaryAsync();
     
            synchronized (mUpdatingLock) {
                mUpdatingDictionary = false;
            }
            return null;
        }
    }

	private void loadDictionaryAsync() {
		try
		{
			AndroidUserDictionary androidBuiltIn = new AndroidUserDictionary(mAnyContext, mLocale);
			androidBuiltIn.loadDictionary();
			mActualDictionary = androidBuiltIn;
		}
		catch(Exception e)
		{
			Log.w(TAG, "Failed to load Android's built-in user dictionary. No matter, I'll use a fallback.");
			FallbackUserDictionary fallback = new FallbackUserDictionary(mAnyContext);
			fallback.loadDictionary();
			
			mActualDictionary = fallback;
		}
	}
    
    public void loadDictionary() {
        synchronized (mUpdatingLock) {
        	if (!mUpdatingDictionary ) {
                mUpdatingDictionary = true;
                new LoadDictionaryTask().execute();
            }
        }
    }
    
    @Override
    public void addWord(String word, int frequency) {
    	if (mActualDictionary != null)
			mActualDictionary.addWord(word, frequency);
    }

}
