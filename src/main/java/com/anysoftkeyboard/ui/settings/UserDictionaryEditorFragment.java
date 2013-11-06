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

package com.anysoftkeyboard.ui.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.dictionaries.WordsCursor;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.AsyncTaskWithProgressWindow;
import net.evendanan.pushingpixels.FragmentChauffeurActivity;

import java.util.ArrayList;

public class UserDictionaryEditorFragment extends ListFragment implements AsyncTaskWithProgressWindow.AsyncTaskOwner {

    private Dialog mDialog;

    static final class DictionaryLocale {
        private final String mLocale;
        private final String mLocaleName;

        public DictionaryLocale(String locale, String name) {
            mLocale = locale;
            mLocaleName = name;
        }

        public String getLocale() { return mLocale; }

        @Override
        public String toString() {
            return String.format("%s - (%s)", mLocaleName, mLocale);
        }

        @Override
        public int hashCode() {
            return mLocale == null? 0 : mLocale.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof DictionaryLocale) {
                String otherLocale = ((DictionaryLocale)o).getLocale();
                if (otherLocale == null && mLocale == null)
                    return true;
                else if (otherLocale == null)
                    return false;
                else if (mLocale == null)
                    return false;
                else
                    return mLocale.equals(otherLocale);
            } else {
                return false;
            }
        }
    }

    public static final String ASK_USER_WORDS_SDCARD_FILENAME = "UserWords.xml";

    static final int DIALOG_SAVE_SUCCESS = 10;
    static final int DIALOG_SAVE_FAILED = 11;

    static final int DIALOG_LOAD_SUCCESS = 20;
    static final int DIALOG_LOAD_FAILED = 21;

    static final String TAG = "ASK_UDE";

    Spinner mLangs;

    WordsCursor mCursor;
    private String mSelectedLocale = null;
    UserDictionary mCurrentDictionary;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        View v = inflater.inflate(R.layout.words_editor_actionbar_view, null);
        mLangs = (Spinner) v.findViewById(R.id.user_dictionay_langs);
        actionBar.setCustomView(v);

        return inflater.inflate(R.layout.user_dictionary_editor, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLangs.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                mSelectedLocale = ((DictionaryLocale) arg0.getItemAtPosition(arg2)).getLocale();
                fillWordsList();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                Log.d(TAG, "No locale selected");
                mSelectedLocale = null;
            }
        });

        TextView emptyView = (TextView) view.findViewById(R.id.empty_user_dictionary);
        emptyView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                createEmptyItemForAdd();
            }
        });

        ListView listView = getListView();
        listView.setFastScrollEnabled(true);
        listView.setEmptyView(emptyView);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.words_editor_menu_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.add_user_word:
                createEmptyItemForAdd();
                return true;
            case R.id.backup_words:
                new BackupUserWordsAsyncTask(UserDictionaryEditorFragment.this).execute();
                return true;
            case R.id.restore_words:
                new RestoreUserWordsAsyncTask(UserDictionaryEditorFragment.this).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createEmptyItemForAdd() {
        //TODO: will create an empty item on the list, and put it in EDIT mode.
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(getString(R.string.user_dict_settings_titlebar));
        fillLanguagesSpinner();
    }


    @Override
    public void onDestroy() {
        FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setCustomView(null);

        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
        mDialog = null;

        super.onDestroy();
        if (mCursor != null)
            mCursor.close();
        if (mCurrentDictionary != null)
            mCurrentDictionary.close();

        mCursor = null;
        mCurrentDictionary = null;
    }

    void fillLanguagesSpinner() {
        new UserWordsEditorAsyncTask(this) {
            private ArrayList<DictionaryLocale> mLanguagesList;

            @Override
            protected Void doAsyncTask(Void[] params) throws Exception {
                mLanguagesList = new ArrayList<DictionaryLocale>();

                ArrayList<KeyboardAddOnAndBuilder> keyboards = KeyboardFactory
                        .getAllAvailableKeyboards(getActivity().getApplicationContext());
                for (KeyboardAddOnAndBuilder kbd : keyboards) {
                    String locale = kbd.getKeyboardLocale();
                    if (TextUtils.isEmpty(locale))
                        continue;

                    DictionaryLocale dictionaryLocale = new DictionaryLocale(locale, kbd.getName());
                    //Don't worry, DictionaryLocale equals any DictionaryLocale with the same locale (no matter what its name is)
                    if (mLanguagesList.contains(dictionaryLocale))
                        continue;
                    Log.d(TAG, "Adding locale " + locale + " to editor.");
                    mLanguagesList.add(dictionaryLocale);
                }
                return null;
            }

            @Override
            protected void applyResults(Void result,
                                        Exception backgroundException) {
                ArrayAdapter<DictionaryLocale> adapter = new ArrayAdapter<DictionaryLocale>(
                        getActivity(),
                        android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                for (DictionaryLocale lang : mLanguagesList)
                    adapter.add(lang);

                mLangs.setAdapter(adapter);
            }

            ;
        }.execute();
    }

    public void showDialog(int id) {
        mDialog = onCreateDialog(id);
        mDialog.show();
    }

    private Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_SAVE_SUCCESS:
                return createDialogAlert(R.string.user_dict_backup_success_title,
                        R.string.user_dict_backup_success_text);
            case DIALOG_SAVE_FAILED:
                return createDialogAlert(R.string.user_dict_backup_fail_title,
                        R.string.user_dict_backup_fail_text);
            case DIALOG_LOAD_SUCCESS:
                return createDialogAlert(R.string.user_dict_restore_success_title,
                        R.string.user_dict_restore_success_text);
            case DIALOG_LOAD_FAILED:
                return createDialogAlert(R.string.user_dict_restore_fail_title,
                        R.string.user_dict_restore_fail_text);
        }

        return null;
    }

    private Dialog createDialogAlert(int title, int text) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        return dialog;
    }

    private void onAddOrEditFinished(String word) {
        if (!TextUtils.isEmpty(word)) {
            // Disallow duplicates
            deleteWord(word);

            mCurrentDictionary.addWord(word, 128);
        }
        fillWordsList();
    }

    void deleteWord(String word) {
        mCurrentDictionary.deleteWord(word);
    }

    public void fillWordsList() {
        Log.d(TAG, "Selected locale is " + mSelectedLocale);
        new UserWordsEditorAsyncTask(this) {
            private UserDictionary mNewDictionary;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // all the code below can be safely (and must) be called in the
                // UI thread.
                mNewDictionary = new UserDictionary(getActivity().getApplicationContext(), mSelectedLocale);
                if (mNewDictionary != mCurrentDictionary
                        && mCurrentDictionary != null && mCursor != null) {
                    mCurrentDictionary.close();
                }
            }

            @Override
            protected Void doAsyncTask(Void[] params) throws Exception {
                mCurrentDictionary = mNewDictionary;
                mCurrentDictionary.loadDictionary();
                mCursor = mCurrentDictionary.getWordsCursor();
                return null;
            }

            protected void applyResults(Void result,
                                        Exception backgroundException) {
                if (AnyApplication.DEBUG)
                    Log.d(TAG, "Creating a new MyAdapter for the words editor");
                MyAdapter adapter = new MyAdapter();
                setListAdapter(adapter);
            }
        }.execute();
    }

    private class MyAdapter extends SimpleCursorAdapter /*
                                                         * implements
														 * SectionIndexer
														 * (removed because of
														 * issue 903)
														 */ {
        // private AlphabetIndexer mIndexer;
        private final int mWordColumnIndex;

        public MyAdapter() {
            super(getActivity(), R.layout.user_dictionary_word_row,
                    mCursor.getCursor(),
                    new String[]{android.provider.UserDictionary.Words.WORD},
                    new int[]{android.R.id.text1});

            mWordColumnIndex = mCursor.getCursor().getColumnIndexOrThrow(
                    android.provider.UserDictionary.Words.WORD);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup v = (ViewGroup) super.getView(position, convertView,
                    parent);
            final String word = ((Cursor) getItem(position))
                    .getString(mWordColumnIndex);

            v.findViewById(R.id.delete_user_word).setOnClickListener(
                    new OnClickListener() {
                        public void onClick(View v) {
                            deleteWord(word);

                            fillWordsList();
                        }
                    });

            return v;
        }
    }
}
