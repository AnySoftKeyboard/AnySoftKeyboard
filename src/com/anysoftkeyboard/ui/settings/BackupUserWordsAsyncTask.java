package com.anysoftkeyboard.ui.settings;

import java.io.File;
import java.util.ArrayList;

import android.database.Cursor;
import android.os.Environment;
import android.provider.UserDictionary.Words;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.anysoftkeyboard.dictionaries.SafeUserDictionary;
import com.anysoftkeyboard.dictionaries.WordsCursor;
import com.anysoftkeyboard.utils.XmlWriter;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

final class BackupUserWordsAsyncTask extends UserWordsEditorAsyncTask {
    private static final String TAG = "ASK BackupUDict";
	/**
	 * 
	 */
	private final UserDictionaryEditorActivity mUserDictionaryEditorActivity;
	ArrayList<String> mLocalesToSave = new ArrayList<String>();
    Exception mException = null;
	private String mLocale;
	private SafeUserDictionary mDictionary;

    BackupUserWordsAsyncTask(UserDictionaryEditorActivity userDictionaryEditorActivity) {
        super(userDictionaryEditorActivity);
		mUserDictionaryEditorActivity = userDictionaryEditorActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // I can access the UI object in the UI thread.
        for (int i = 0; i < mUserDictionaryEditorActivity.mLangs.getCount(); i++)
        {
            final String locale = (String) mUserDictionaryEditorActivity.mLangs.getItemAtPosition(i);
            if (!TextUtils.isEmpty(locale)) {
                mLocalesToSave.add(locale);
                Log.d(TAG, "Found a locale to backup: " + locale);
            }
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        // out of UI thread.
        try
        {
            // http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
            final File externalFolder = Environment.getExternalStorageDirectory();
            final File targetFolder = new File(externalFolder, "/Android/data/"
                    + mUserDictionaryEditorActivity.getPackageName() + "/files/");
            targetFolder.mkdirs();
            // https://github.com/menny/Java-very-tiny-XmlWriter/blob/master/XmlWriter.java
            XmlWriter output = new XmlWriter(new File(targetFolder,
                    UserDictionaryEditorActivity.ASK_USER_WORDS_SDCARD_FILENAME));

            output.writeEntity("userwordlist");
            for (String locale : mLocalesToSave) {
            	mLocale = locale;
                synchronized (mLocale) {
                    Log.d(TAG, "Building dictionary for locale " + mLocale);
					publishProgress();
					//waiting for dictionary to be ready.
					try {
						mLocale.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
                Log.d(TAG, "Reading words from user dictionary locale " + locale);
                WordsCursor wordsCursor = mDictionary.getWordsCursor();

                output.writeEntity("wordlist").writeAttribute("locale", locale);
                Cursor cursor = wordsCursor.getCursor();
                cursor.moveToFirst();
                final int wordIndex = cursor.getColumnIndex(Words.WORD);
                final int freqIndex = cursor.getColumnIndex(Words.FREQUENCY);

                while (!cursor.isAfterLast()) {
                    String word = cursor.getString(wordIndex).trim();
                    int freq = cursor.getInt(freqIndex);
                    // <w f="128">Facebook</w>
                    output.writeEntity("w").writeAttribute("f", Integer.toString(freq))
                            .writeText(word).endEntity();
                    if (AnyApplication.DEBUG)
                        Log.d(TAG, "Storing word '" + word + "' with freq " + freq);
                    cursor.moveToNext();
                }

                wordsCursor.close();
                mDictionary.close();
                
                output.endEntity();// wordlist
            }

            output.endEntity();// userwordlist
            output.close();
        } catch (Exception e)
        {
            mException = e;
            e.printStackTrace();
        }

        return null;
    }
    
    @Override
    protected void onProgressUpdate(Void... values) {
    	super.onProgressUpdate(values);
    	synchronized (mLocale) {
    		mDictionary = new SafeUserDictionary(mUserDictionaryEditorActivity.getApplicationContext(), mLocale);
    		mDictionary.loadDictionarySync();
    		mLocale.notifyAll();
    	}
    }

    @Override
    protected void applyResults(Void result) {
        if (mException != null) {
            Toast.makeText(
                    mUserDictionaryEditorActivity.getApplicationContext(),
                    mUserDictionaryEditorActivity.getString(R.string.user_dict_backup_fail_text_with_error,
                            mException.getMessage()), Toast.LENGTH_LONG).show();
            mUserDictionaryEditorActivity.showDialog(UserDictionaryEditorActivity.DIALOG_SAVE_FAILED);
        } else {
            mUserDictionaryEditorActivity.showDialog(UserDictionaryEditorActivity.DIALOG_SAVE_SUCCESS);
        }
        // re-reading words (this is a simple way to re-sync the
        // dictionary members)
        mUserDictionaryEditorActivity.fillLangsSpinner();
    }
}