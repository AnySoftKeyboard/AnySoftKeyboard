package com.anysoftkeyboard.ui.settings;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.anysoftkeyboard.dictionaries.SafeUserDictionary;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

final class RestoreUserWordsAsyncTask extends UserWordsEditorAsyncTask {
    protected static final String TAG = "ASK RestoreUDict";
	/**
	 * 
	 */
	private final UserDictionaryEditorActivity mUserDictionaryEditorActivity;
	private Exception mException = null;
	private String mLocale;
	private SafeUserDictionary mDictionary;

    RestoreUserWordsAsyncTask(UserDictionaryEditorActivity userDictionaryEditorActivity) {
        super(userDictionaryEditorActivity);
		mUserDictionaryEditorActivity = userDictionaryEditorActivity;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try
        {
            // http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
            final File externalFolder = Environment.getExternalStorageDirectory();
            final File targetFolder = new File(externalFolder, "/Android/data/"
                    + mUserDictionaryEditorActivity.getPackageName() + "/files/");

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(new FileInputStream(new File(targetFolder,
                    UserDictionaryEditorActivity.ASK_USER_WORDS_SDCARD_FILENAME)),
                    new DefaultHandler() {
                        private boolean inWord = false;
                        private int freq = 1;
                        private String word = "";

                        @Override
                        public void characters(char[] ch, int start, int length)
                                throws SAXException {
                            super.characters(ch, start, length);
                            if (inWord)
                            {
                                word += new String(ch, start, length);
                            }
                        }

                        @Override
                        public void startElement(String uri, String localName,
                                String qName, Attributes attributes) throws SAXException {
                            super.startElement(uri, localName, qName, attributes);
                            if (localName.equals("w")) {
                                inWord = true;
                                word = "";
                                freq = Integer.parseInt(attributes.getValue("f"));
                            }

                            if (localName.equals("wordlist")) {
                                mLocale = attributes.getValue("locale");
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
                                
                                Log.d(TAG, "Starting restore to locale " + mLocale);
                            }
                        }

                        @Override
                        public void endElement(String uri, String localName, String qName)
                                throws SAXException {
                            if (inWord && localName.equals("w")) {
                                if (!TextUtils.isEmpty(word)) {
                                    if (AnyApplication.DEBUG)
                                        Log.d(TAG, "Restoring word '" + word + "' with freq "
                                                + freq);
                                    // Disallow duplicates
                                    mDictionary.deleteWord(word);
                                    mDictionary.addWord(word, freq);
                                }

                                inWord = false;
                            }
                            super.endElement(uri, localName, qName);
                        }
                    });
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
    		if (mDictionary != null) {
    			mDictionary.close();
    		}
    		mDictionary = new SafeUserDictionary(mUserDictionaryEditorActivity.getApplicationContext(), mLocale);
    		mDictionary.loadDictionarySync();
    		mLocale.notifyAll();
    	}
    }

    @Override
    protected void applyResults(Void result) {
    	if (mDictionary != null) {
			mDictionary.close();
		}
    	
        if (mException != null) {
            Toast.makeText(
                    mUserDictionaryEditorActivity.getApplicationContext(),
                    mUserDictionaryEditorActivity.getString(R.string.user_dict_restore_fail_text_with_error,
                            mException.getMessage()), Toast.LENGTH_LONG).show();
            mUserDictionaryEditorActivity.showDialog(UserDictionaryEditorActivity.DIALOG_LOAD_FAILED);
        } else {
            mUserDictionaryEditorActivity.showDialog(UserDictionaryEditorActivity.DIALOG_LOAD_SUCCESS);
        }
        // re-reading words (this is a simple way to re-sync the
        // dictionary members)
        mUserDictionaryEditorActivity.fillLangsSpinner();
    }
}