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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.anysoftkeyboard.dictionaries.EditableDictionary;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.dictionaries.WordsCursor;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.AsyncTaskWithProgressWindow;
import net.evendanan.pushingpixels.FragmentChauffeurActivity;
import net.evendanan.pushingpixels.PassengerFragmentSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserDictionaryEditorFragment extends Fragment
        implements AsyncTaskWithProgressWindow.AsyncTaskOwner, EditorWordsAdapter.DictionaryCallbacks {

    private Dialog mDialog;

    private static final String ASK_USER_WORDS_SDCARD_FILENAME = "UserWords.xml";

    static final int DIALOG_SAVE_SUCCESS = 10;
    static final int DIALOG_SAVE_FAILED = 11;

    static final int DIALOG_LOAD_SUCCESS = 20;
    static final int DIALOG_LOAD_FAILED = 21;

    static final String TAG = "ASK_UDE";

    Spinner mLanguagesSpinner;

    WordsCursor mCursor;
    private String mSelectedLocale = null;
    EditableDictionary mCurrentDictionary;

    RecyclerView mWordsRecyclerView;

    private static final Comparator<EditorWord> msWordsComparator = new Comparator<EditorWord>() {
        @Override
        public int compare(EditorWord lhs, EditorWord rhs) {
            return lhs.word.compareTo(rhs.word);
        }
    };

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

        mWordsRecyclerView = (RecyclerView) view.findViewById(R.id.words_recycler_view);
        mWordsRecyclerView.setHasFixedSize(false);
        final int wordsEditorColumns = getResources().getInteger(R.integer.words_editor_columns_count);
        if (wordsEditorColumns > 1) {
            mWordsRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), wordsEditorColumns));
        } else {
            mWordsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
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
                new BackupUserWordsAsyncTask(UserDictionaryEditorFragment.this, ASK_USER_WORDS_SDCARD_FILENAME).execute();
                return true;
            case R.id.restore_words:
                new RestoreUserWordsAsyncTask(UserDictionaryEditorFragment.this, ASK_USER_WORDS_SDCARD_FILENAME).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createEmptyItemForAdd() {
        EditorWordsAdapter adapter = (EditorWordsAdapter) mWordsRecyclerView.getAdapter();
        if (adapter == null || !isResumed()) return;
        adapter.addNewWordAtEnd(mWordsRecyclerView);
    }

    @Override
    public void onStart() {
        super.onStart();
        PassengerFragmentSupport.setActivityTitle(this, getString(R.string.user_dict_settings_titlebar));
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
        new UserWordsEditorAsyncTask(this, true) {
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

                List<KeyboardAddOnAndBuilder> keyboards = KeyboardFactory.getEnabledKeyboards(getActivity().getApplicationContext());
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
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }

    private void fillWordsList() {
        Log.d(TAG, "Selected locale is " + mSelectedLocale);
        new UserWordsEditorAsyncTask(this, true) {
            private EditableDictionary mNewDictionary;
            private List<EditorWord> mWordsList;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // all the code below can be safely (and must) be called in the
                // UI thread.
                mNewDictionary = getEditableDictionary(mSelectedLocale);
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
                while (!cursor.isAfterLast()) {
                    EditorWord word = new EditorWord(
                            mCursor.getCurrentWord(),
                            mCursor.getCurrentWordFrequency());
                    mWordsList.add(word);
                    cursor.moveToNext();
                }
                //now, sorting the word list alphabetically
                Collections.sort(mWordsList, msWordsComparator);
                return null;
            }

            protected void applyResults(Void result, Exception backgroundException) {
                mWordsRecyclerView.setAdapter(createAdapterForWords(mWordsList));
            }
        }.execute();
    }

    protected EditorWordsAdapter createAdapterForWords(List<EditorWord> wordsList) {
        return new EditorWordsAdapter(wordsList, LayoutInflater.from(getActivity()), this);
    }

    protected EditableDictionary getEditableDictionary(String locale) {
        return new UserDictionary(getActivity().getApplicationContext(), locale);
    }

    @Override
    public void onWordDeleted(final EditorWord word) {
        new UserWordsEditorAsyncTask(this, false) {
            @Override
            protected Void doAsyncTask(Void[] params) throws Exception {
                deleteWord(word.word);
                return null;
            }

            @Override
            protected void applyResults(Void aVoid, Exception backgroundException) {
            }
        }.execute();
    }

    private void deleteWord(String word) {
        mCurrentDictionary.deleteWord(word);
    }

    @Override
    public void onWordUpdated(final String oldWord, final EditorWord newWord) {

        new UserWordsEditorAsyncTask(this, false) {
            @Override
            protected Void doAsyncTask(Void[] params) throws Exception {
                if (!TextUtils.isEmpty(oldWord))//it can be empty in case it's a new word.
                    deleteWord(oldWord);
                deleteWord(newWord.word);
                mCurrentDictionary.addWord(newWord.word, newWord.frequency);
                return null;
            }

            @Override
            protected void applyResults(Void aVoid, Exception backgroundException) {
            }
        }.execute();
    }
}
