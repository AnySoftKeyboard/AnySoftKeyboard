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
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.dictionaries.content.AndroidUserDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.FallbackUserDictionary;
import com.anysoftkeyboard.utils.Logger;
import com.anysoftkeyboard.utils.XmlWriter;
import com.menny.android.anysoftkeyboard.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

final class BackupUserWordsAsyncTask extends UserWordsEditorAsyncTask {
    private static final String TAG = "ASK BackupUDict";

    private final String mFilename;

    private final ArrayList<String> mLocalesToSave = new ArrayList<>();

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
        for (int i = 0; i < a.getLanguagesSpinner().getCount(); i++) {
            final String locale = ((DictionaryLocale) a.getLanguagesSpinner().getItemAtPosition(i)).getLocale();
            if (!TextUtils.isEmpty(locale)) {
                mLocalesToSave.add(locale);
                Logger.d(TAG, "Found a locale to backup: " + locale);
            }
        }
    }

    @Override
    protected Void doAsyncTask(Void[] params) throws Exception {
        // http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
        final File externalFolder = Environment.getExternalStorageDirectory();
        final File targetFolder = new File(externalFolder, "/Android/data/" + mAppContext.getPackageName() + "/files/");
        targetFolder.mkdirs();
        // https://github.com/menny/Java-very-tiny-XmlWriter/blob/master/XmlWriter.java
        XmlWriter output = new XmlWriter(new File(targetFolder, mFilename));

        output.writeEntity("userwordlist");
        for (String locale : mLocalesToSave) {
            Logger.d(TAG, "Building dictionary for locale " + locale);
            MyUserDictionary dictionary = new MyUserDictionary(mAppContext, locale);
            dictionary.loadDictionary();
            Logger.d(TAG, "Reading words from user dictionary locale " + locale);

            output.writeEntity("wordlist").writeAttribute("locale", locale);
            List<UserDictionaryEditorFragment.LoadedWord> words = dictionary.getLoadedWords();

            for (UserDictionaryEditorFragment.LoadedWord word : words) {
                // <w f="128">Facebook</w>
                output.writeEntity("w")
                        .writeAttribute("f", Integer.toString(word.freq))
                        .writeText(word.word).endEntity();
            }

            dictionary.close();

            output.endEntity();// wordlist
        }

        output.endEntity();// userwordlist
        output.close();

        return null;
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
            if (a != null && a.isVisible())
                a.showDialog(UserDictionaryEditorFragment.DIALOG_SAVE_FAILED);
        } else {
            if (a != null && a.isVisible())
                a.showDialog(UserDictionaryEditorFragment.DIALOG_SAVE_SUCCESS);
        }
        // re-reading words (this is a simple way to re-sync the
        // dictionary members)
        if (a != null && a.isAdded())
            a.fillLanguagesSpinner();
    }

    private static class MyUserDictionary extends UserDictionary implements UserDictionaryEditorFragment.MyEditableDictionary {

        public MyUserDictionary(Context context, String locale) {
            super(context, locale);
        }

        @NonNull
        @Override
        public List<UserDictionaryEditorFragment.LoadedWord> getLoadedWords() {
            return ((UserDictionaryEditorFragment.MyEditableDictionary) super.getActualDictionary()).getLoadedWords();
        }

        @NonNull
        @Override
        protected AndroidUserDictionary createAndroidUserDictionary(Context context, String locale) {
            return new MyAndroidUserDictionary(context, locale);
        }

        @NonNull
        @Override
        protected FallbackUserDictionary createFallbackUserDictionary(Context context, String locale) {
            return new MyFallbackUserDictionary(context, locale);
        }
    }

    private static class MyFallbackUserDictionary extends FallbackUserDictionary implements UserDictionaryEditorFragment.MyEditableDictionary {

        @NonNull
        private List<UserDictionaryEditorFragment.LoadedWord> mLoadedWords = new ArrayList<>();

        public MyFallbackUserDictionary(Context context, String locale) {
            super(context, locale);
        }

        @Override
        protected void readWordsFromActualStorage(final WordReadListener listener) {
            mLoadedWords.clear();
            WordReadListener myListener = new WordReadListener() {
                @Override
                public boolean onWordRead(String word, int frequency) {
                    mLoadedWords.add(new UserDictionaryEditorFragment.LoadedWord(word, frequency));
                    return listener.onWordRead(word, frequency);
                }
            };
            super.readWordsFromActualStorage(myListener);
        }

        @NonNull
        @Override
        public List<UserDictionaryEditorFragment.LoadedWord> getLoadedWords() {
            return mLoadedWords;
        }
    }

    private static class MyAndroidUserDictionary extends AndroidUserDictionary implements UserDictionaryEditorFragment.MyEditableDictionary {

        @NonNull
        private List<UserDictionaryEditorFragment.LoadedWord> mLoadedWords = new ArrayList<>();

        public MyAndroidUserDictionary(Context context, String locale) {
            super(context, locale);
        }

        @Override
        protected void readWordsFromActualStorage(final WordReadListener listener) {
            mLoadedWords.clear();
            WordReadListener myListener = new WordReadListener() {
                @Override
                public boolean onWordRead(String word, int frequency) {
                    mLoadedWords.add(new UserDictionaryEditorFragment.LoadedWord(word, frequency));
                    return listener.onWordRead(word, frequency);
                }
            };
            super.readWordsFromActualStorage(myListener);
        }

        @NonNull
        @Override
        public List<UserDictionaryEditorFragment.LoadedWord> getLoadedWords() {
            return mLoadedWords;
        }
    }
}