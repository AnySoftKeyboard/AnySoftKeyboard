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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
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
import java.util.List;

public class UserDictionaryEditorFragment extends Fragment
        implements AsyncTaskWithProgressWindow.AsyncTaskOwner, AdapterView.OnItemClickListener, UserWordsListAdapter.AdapterCallbacks {

    private Dialog mDialog;

    public static final String ASK_USER_WORDS_SDCARD_FILENAME = "UserWords.xml";

    static final int DIALOG_SAVE_SUCCESS = 10;
    static final int DIALOG_SAVE_FAILED = 11;

    static final int DIALOG_LOAD_SUCCESS = 20;
    static final int DIALOG_LOAD_FAILED = 21;

    static final String TAG = "ASK_UDE";

    Spinner mLanguagesSpinner;

    WordsCursor mCursor;
    private String mSelectedLocale = null;
    UserDictionary mCurrentDictionary;

    AbsListView mWordsListView;//this may be either ListView or GridView (in tablets)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        View v = inflater.inflate(R.layout.words_editor_actionbar_view, null);
        mLanguagesSpinner = (Spinner) v.findViewById(R.id.user_dictionay_langs);
        actionBar.setCustomView(v);

        return inflater.inflate(R.layout.user_dictionary_editor, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLanguagesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
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

        mWordsListView = (AbsListView) view.findViewById(android.R.id.list);
        mWordsListView.setFastScrollEnabled(true);
        //this is for the "empty state" - it will allow the user to quickly add the first word.
        mWordsListView.setEmptyView(emptyView);
        mWordsListView.setOnItemClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.words_editor_menu_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        UserWordsListAdapter adapter = (UserWordsListAdapter) mWordsListView.getAdapter();
        final int addWordItemIndex = adapter.getCount() == 0? 0 : adapter.getCount()-1;
        //will use smooth scrolling on API8+
        AnyApplication.getDeviceSpecific().performListScrollToPosition(mWordsListView, addWordItemIndex);
        onItemClick(mWordsListView, null, addWordItemIndex, 0l);
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
            private ArrayAdapter<DictionaryLocale> mAdapter;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //creating in the UI thread
                mAdapter = new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.simple_spinner_item);
            }

            @Override
            protected Void doAsyncTask(Void[] params) throws Exception {
                ArrayList<DictionaryLocale> languagesList = new ArrayList<>();

                ArrayList<KeyboardAddOnAndBuilder> keyboards = KeyboardFactory
                        .getAllAvailableKeyboards(getActivity().getApplicationContext());
                for (KeyboardAddOnAndBuilder kbd : keyboards) {
                    String locale = kbd.getKeyboardLocale();
                    if (TextUtils.isEmpty(locale))
                        continue;

                    DictionaryLocale dictionaryLocale = new DictionaryLocale(locale, kbd.getName());
                    //Don't worry, DictionaryLocale equals any DictionaryLocale with the same locale (no matter what its name is)
                    if (languagesList.contains(dictionaryLocale))
                        continue;
                    Log.d(TAG, "Adding locale " + locale + " to editor.");
                    languagesList.add(dictionaryLocale);
                }

                mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                for (DictionaryLocale lang : languagesList)
                    mAdapter.add(lang);

                return null;
            }

            @Override
            protected void applyResults(Void result, Exception backgroundException) {
                mLanguagesSpinner.setAdapter(mAdapter);
            }
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

    private void addWord(String word) {
        deleteWord(word);// Disallow duplicates
        mCurrentDictionary.addWord(word, 128);
    }

    private void deleteWord(String word) {
        mCurrentDictionary.deleteWord(word);
    }

    public void fillWordsList() {
        Log.d(TAG, "Selected locale is " + mSelectedLocale);
        new UserWordsEditorAsyncTask(this) {
            private UserDictionary mNewDictionary;
            private List<String> mWordsList;

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
                Cursor cursor = mCursor.getCursor();
                mWordsList = new ArrayList<>(mCursor.getCursor().getCount());
                cursor.moveToFirst();
                while(!cursor.isAfterLast()) {
                    mWordsList.add(mCursor.getCurrentWord());
                    cursor.moveToNext();
                }
                return null;
            }

            protected void applyResults(Void result,
                                        Exception backgroundException) {
                if (AnyApplication.DEBUG)
                    Log.d(TAG, "Creating a new UserWordsListAdapter for the words editor");
                UserWordsListAdapter adapter = new UserWordsListAdapter(
                        UserDictionaryEditorFragment.this.getActivity(),
                        mWordsList,
                        UserDictionaryEditorFragment.this);
                mWordsListView.setAdapter(adapter);
            }
        }.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((UserWordsListAdapter) mWordsListView.getAdapter()).onItemClicked(parent, position);
    }

    @Override
    public void onWordDeleted(final String word) {
        new UserWordsEditorAsyncTask(this) {
            @Override
            protected Void doAsyncTask(Void[] params) throws Exception {
                deleteWord(word);
                return null;
            }

            @Override
            protected void applyResults(Void aVoid, Exception backgroundException) {
                fillWordsList();
            }
        }.execute();
    }

    @Override
    public void onWordUpdated(final String oldWord, final String newWord) {

        new UserWordsEditorAsyncTask(this) {
            @Override
            protected Void doAsyncTask(Void[] params) throws Exception {
                if (!TextUtils.isEmpty(oldWord))//it can be empty in case it's a new word.
                    deleteWord(oldWord);
                addWord(newWord);
                return null;
            }

            @Override
            protected void applyResults(Void aVoid, Exception backgroundException) {
                fillWordsList();
            }
        }.execute();
    }

    @Override
    public void performDiscardEdit() {
        ((UserWordsListAdapter) mWordsListView.getAdapter()).onItemClicked(mWordsListView, -1/*doesn't really matter what position it is*/);
    }
}
