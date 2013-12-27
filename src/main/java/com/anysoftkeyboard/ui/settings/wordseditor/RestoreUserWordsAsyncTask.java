/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ui.settings.wordseditor;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.view.WindowManager.BadTokenException;
import android.widget.Toast;

import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

final class RestoreUserWordsAsyncTask extends UserWordsEditorAsyncTask {
    protected static final String TAG = "ASK RestoreUDict";

    private final Context mAppContext;
    private final String mFilename;
    private String mLocale;
    private UserDictionary mDictionary;

    RestoreUserWordsAsyncTask(
            UserDictionaryEditorFragment callingFragment, String filename) {
        super(callingFragment);
        mAppContext = callingFragment.getActivity().getApplicationContext();
        mFilename = filename;
    }

    @Override
    protected Void doAsyncTask(Void[] params) throws Exception {
        // http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
        final File externalFolder = Environment.getExternalStorageDirectory();
        final File targetFolder = new File(externalFolder, "/Android/data/"
                + mAppContext.getPackageName() + "/files/");

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(new FileInputStream(new File(targetFolder, mFilename)),
                new DefaultHandler() {
                    private boolean inWord = false;
                    private int freq = 1;
                    private String word = "";

                    @Override
                    public void characters(char[] ch, int start, int length)
                            throws SAXException {
                        super.characters(ch, start, length);
                        if (inWord) {
                            word += new String(ch, start, length);
                        }
                    }

                    @Override
                    public void startElement(String uri, String localName,
                                             String qName, Attributes attributes)
                            throws SAXException {
                        super.startElement(uri, localName, qName, attributes);
                        if (localName.equals("w")) {
                            inWord = true;
                            word = "";
                            freq = Integer.parseInt(attributes.getValue("f"));
                        }

                        if (localName.equals("wordlist")) {
                            mLocale = attributes.getValue("locale");
                            synchronized (mLocale) {
                                Log.d(TAG, "Building dictionary for locale "
                                        + mLocale);
                                publishProgress();
                                // waiting for dictionary to be ready.
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
                    public void endElement(String uri, String localName,
                                           String qName) throws SAXException {
                        if (inWord && localName.equals("w")) {
                            if (!TextUtils.isEmpty(word)) {
                                Log.d(TAG, "Restoring word '" + word
                                        + "' with freq " + freq);
                                // Disallow duplicates
                                mDictionary.deleteWord(word);
                                mDictionary.addWord(word, freq);
                            }

                            inWord = false;
                        }
                        super.endElement(uri, localName, qName);
                    }
                });

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        synchronized (mLocale) {
            if (mDictionary != null) {
                mDictionary.close();
            }
            mDictionary = new UserDictionary(mAppContext, mLocale);
            mDictionary.loadDictionary();
            mLocale.notifyAll();
        }
    }

    @Override
    protected void applyResults(Void result, Exception backgroundException) {
        if (mDictionary != null) {
            mDictionary.close();
        }

        UserDictionaryEditorFragment activity = getOwner();

        try {
            if (backgroundException != null) {
                Toast.makeText(
                        mAppContext,
                        mAppContext
                                .getString(
                                        R.string.user_dict_restore_fail_text_with_error,
                                        backgroundException.getMessage()),
                        Toast.LENGTH_LONG).show();
                if (activity != null)
                    activity.showDialog(UserDictionaryEditorFragment.DIALOG_LOAD_FAILED);
            } else {
                if (activity != null)
                    activity.showDialog(UserDictionaryEditorFragment.DIALOG_LOAD_SUCCESS);
            }
            // re-reading words (this is a simple way to re-sync the
            // dictionary members)
            if (activity != null)
                activity.fillLanguagesSpinner();
        } catch (BadTokenException e) {
            // activity gone away!
            // nevermind
        }
    }
}