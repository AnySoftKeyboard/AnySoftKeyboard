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
import android.database.Cursor;
import android.os.Environment;
import android.provider.UserDictionary.Words;
import android.text.TextUtils;
import android.widget.Toast;

import com.anysoftkeyboard.base.dictionaries.WordsCursor;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.utils.Log;
import com.anysoftkeyboard.utils.XmlWriter;
import com.menny.android.anysoftkeyboard.R;

import java.io.File;
import java.util.ArrayList;

final class BackupUserWordsAsyncTask extends UserWordsEditorAsyncTask {
    private static final String TAG = "ASK BackupUDict";

    private final String mFilename;

    ArrayList<String> mLocalesToSave = new ArrayList<String>();

    private String mLocale;
    private UserDictionary mDictionary;
    private final Context mAppContext;

    BackupUserWordsAsyncTask(UserDictionaryEditorFragment callingFragment, String filename) {
        super(callingFragment, true);
        mAppContext = callingFragment.getActivity().getApplicationContext();
        mFilename = filename;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        UserDictionaryEditorFragment a = getOwner();
        if (a == null)
            return;
        // I can access the UI object in the UI thread.
        for (int i = 0; i < a.mLanguagesSpinner.getCount(); i++) {
            final String locale = ((DictionaryLocale) a.mLanguagesSpinner.getItemAtPosition(i)).getLocale();
            if (!TextUtils.isEmpty(locale)) {
                mLocalesToSave.add(locale);
                Log.d(TAG, "Found a locale to backup: " + locale);
            }
        }
    }

    @Override
    protected Void doAsyncTask(Void[] params) throws Exception {
        // http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
        final File externalFolder = Environment.getExternalStorageDirectory();
        final File targetFolder = new File(externalFolder, "/Android/data/"
                + mAppContext.getPackageName() + "/files/");
        targetFolder.mkdirs();
        // https://github.com/menny/Java-very-tiny-XmlWriter/blob/master/XmlWriter.java
        XmlWriter output = new XmlWriter(new File(targetFolder, mFilename));

        output.writeEntity("userwordlist");
        for (String locale : mLocalesToSave) {
            mLocale = locale;
            synchronized (mLocale) {
                Log.d(TAG, "Building dictionary for locale " + mLocale);
                publishProgress();
                // waiting for dictionary to be ready.
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
                output.writeEntity("w")
                        .writeAttribute("f", Integer.toString(freq))
                        .writeText(word).endEntity();
                Log.d(TAG, "Storing word '" + word + "' with freq " + freq);
                cursor.moveToNext();
            }

            wordsCursor.close();
            mDictionary.close();

            output.endEntity();// wordlist
        }

        output.endEntity();// userwordlist
        output.close();

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        synchronized (mLocale) {
            mDictionary = new UserDictionary(mAppContext, mLocale);
            mDictionary.loadDictionary();
            mLocale.notifyAll();
        }
    }

    @Override
    protected void applyResults(Void result, Exception backgroundException) {
        UserDictionaryEditorFragment a = getOwner();
        if (backgroundException != null) {
            Toast.makeText(
                    mAppContext,
                    mAppContext.getString(
                            R.string.user_dict_backup_fail_text_with_error,
                            backgroundException.getMessage()), Toast.LENGTH_LONG).show();
            if (a != null)
                a.showDialog(UserDictionaryEditorFragment.DIALOG_SAVE_FAILED);
        } else {
            if (a != null)
                a.showDialog(UserDictionaryEditorFragment.DIALOG_SAVE_SUCCESS);
        }
        // re-reading words (this is a simple way to re-sync the
        // dictionary members)
        if (a != null)
            a.fillLanguagesSpinner();
    }
}