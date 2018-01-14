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
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.WindowManager.BadTokenException;
import android.widget.Toast;

import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.R;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

final class RestoreUserWordsAsyncTask extends UserWordsEditorAsyncTask {
    private static final String TAG = "ASK RestoreUDict";

    private final String mFilename;
    private String mLocale;
    private UserDictionary mDictionary;

    RestoreUserWordsAsyncTask(UserDictionaryEditorFragment callingFragment, String filename) {
        super(callingFragment, true);
        mFilename = filename;
    }

    @Override
    protected Void doAsyncTask(Void[] params) throws Exception {
        final Fragment owner = getOwner();
        if (owner == null) return null;

        final Context context = owner.getContext();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        final FileInputStream fileInputStream = new FileInputStream(new File(getBackupFolder(context), mFilename));
        try {
            parser.parse(fileInputStream,
                    new DefaultHandler() {
                        private boolean mInWord = false;
                        private int mFreq = 1;
                        private String mWord = "";

                        @Override
                        public void characters(char[] ch, int start, int length)
                                throws SAXException {
                            super.characters(ch, start, length);
                            if (mInWord) {
                                mWord += new String(ch, start, length);
                            }
                        }

                        @Override
                        public void startElement(String uri, String localName,
                                                 String qualifiedName, Attributes attributes)
                                throws SAXException {
                            super.startElement(uri, localName, qualifiedName, attributes);
                            if (localName.equals("w")) {
                                mInWord = true;
                                mWord = "";
                                mFreq = Integer.parseInt(attributes.getValue("f"));
                            }

                            if (localName.equals("wordlist")) {
                                mLocale = attributes.getValue("locale");
                                Logger.d(TAG, "Building dictionary for locale " + mLocale);
                                if (mDictionary != null) {
                                    mDictionary.close();
                                }
                                mDictionary = new UserDictionary(context, mLocale);
                                mDictionary.loadDictionary();

                                Logger.d(TAG, "Starting restore to locale " + mLocale);
                            }
                        }

                        @Override
                        public void endElement(String uri, String localName,
                                               String qualifiedName) throws SAXException {
                            if (mInWord && localName.equals("w")) {
                                if (!TextUtils.isEmpty(mWord)) {
                                    Logger.d(TAG, "Restoring mWord '" + mWord
                                            + "' with mFreq " + mFreq);
                                    // Disallow duplicates
                                    mDictionary.deleteWord(mWord);
                                    mDictionary.addWord(mWord, mFreq);
                                }

                                mInWord = false;
                            }
                            super.endElement(uri, localName, qualifiedName);
                        }
                    });
        } finally {
            fileInputStream.close();
        }

        return null;
    }

    @Override
    protected void applyResults(Void result, Exception backgroundException) {
        if (mDictionary != null) {
            mDictionary.close();
        }

        UserDictionaryEditorFragment owner = getOwner();
        if (owner == null) return;
        final Context context = owner.getContext();

        try {
            if (backgroundException != null) {
                Toast.makeText(
                        context,
                        context.getString(
                                        R.string.user_dict_restore_fail_text_with_error,
                                        backgroundException.getMessage()),
                        Toast.LENGTH_LONG).show();
                if (owner.isVisible())
                    owner.showDialog(UserDictionaryEditorFragment.DIALOG_LOAD_FAILED);
            } else {
                if (owner.isVisible())
                    owner.showDialog(UserDictionaryEditorFragment.DIALOG_LOAD_SUCCESS);
            }
            // re-reading words (this is a simple way to re-sync the
            // dictionary members)
            if (owner.isAdded())
                owner.fillLanguagesSpinner();
        } catch (BadTokenException e) {
            // owner gone away!
            // never mind
        }
    }
}