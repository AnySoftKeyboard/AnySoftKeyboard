package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.WordComposer;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

public class SafeUserDictionary extends EditableDictionary {

	private static final String TAG = "ASK_SUD";
	private final Object mLocker = new Object();
	private final Context mContext;
	private UserDictionaryBase mActualDictionary;

	private final Object mUpdatingLock = new Object();
	private boolean mUpdatingDictionary = false;
	private final String mLocale;
	
	public SafeUserDictionary(Context context, String locale) {
		super("SafeUserDictionary");
		mLocale = locale;
		mContext = context;
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
		synchronized (mLocker) {
			try
			{
				AndroidUserDictionary androidBuiltIn = new AndroidUserDictionary(mContext, mLocale);
				androidBuiltIn.loadDictionary();
				mActualDictionary = androidBuiltIn;
			}
			catch(Exception e)
			{
				Log.w(TAG, "Failed to load Android's built-in user dictionary. No matter, I'll use a fallback.");
				FallbackUserDictionary fallback = new FallbackUserDictionary(mContext, mLocale);
				fallback.loadDictionary();
				
				mActualDictionary = fallback;
			}
			
			mLocker.notifyAll();
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
    
    @Override
    public Cursor getWordsCursor() {
    	synchronized (mLocker) {
        	if (mActualDictionary != null)
        	{
    			return mActualDictionary.getWordsCursor();
        	}
        	else
        	{
        		try {
					mLocker.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return null;
				}
        		
        		return getWordsCursor();
        	}
    	}
    }
    
    @Override
    public void deleteWord(String word) {
    	if (mActualDictionary != null)
			mActualDictionary.deleteWord(word);
    }

}
