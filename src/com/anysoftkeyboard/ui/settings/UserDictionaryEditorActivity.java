/**
 * Copyright (C) 2009 Google Inc.
 * Copyright (C) 2012 AnySoftKeyboard.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.anysoftkeyboard.ui.settings;

import java.util.ArrayList;



import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.anysoftkeyboard.dictionaries.EditableDictionary;
import com.anysoftkeyboard.dictionaries.SafeUserDictionary;
import com.anysoftkeyboard.dictionaries.WordsCursor;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class UserDictionaryEditorActivity extends ListActivity {

    public static final String ASK_USER_WORDS_SDCARD_FILENAME = "UserWords.xml";

    private static final String INSTANCE_KEY_DIALOG_EDITING_WORD = "DIALOG_EDITING_WORD";
    private static final String INSTANCE_KEY_ADDED_WORD = "DIALOG_ADDED_WORD";

    private static final String EXTRA_WORD = "word";

    private static final int DIALOG_ADD_OR_EDIT = 0;

    static final int DIALOG_SAVE_SUCCESS = 10;
    static final int DIALOG_SAVE_FAILED = 11;

    static final int DIALOG_LOAD_SUCCESS = 20;
    static final int DIALOG_LOAD_FAILED = 21;

    static final String TAG = "ASK_UDE";

    /**
     * The word being edited in the dialog (null means the user is adding a
     * word).
     */
    private String mDialogEditingWord;

    Spinner mLangs;

    WordsCursor mCursor;
    private String mSelectedLocale = null;
    SafeUserDictionary mCurrentDictionary;

    private boolean mAddedWordAlready;
    private boolean mAutoReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_dictionary_editor);

        mLangs = (Spinner) findViewById(R.id.user_dictionay_langs);
        mLangs.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mSelectedLocale = arg0.getItemAtPosition(arg2).toString();
                fillWordsList();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                Log.d(TAG, "No locale selected");
                mSelectedLocale = null;
            }
        });

        findViewById(R.id.add_user_word).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showAddOrEditDialog(null);
            }
        });

        findViewById(R.id.backup_words).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                new BackupUserWordsAsyncTask(UserDictionaryEditorActivity.this).execute();
            }
        });

        findViewById(R.id.restore_words).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                new RestoreUserWordsAsyncTask(UserDictionaryEditorActivity.this).execute();
            }
        });

        TextView emptyView = (TextView) findViewById(R.id.empty_user_dictionary);

        ListView listView = getListView();
        listView.setFastScrollEnabled(true);
        listView.setEmptyView(emptyView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mAddedWordAlready &&
                !TextUtils.isEmpty(getIntent().getAction()) &&
                getIntent().getAction().equals("com.android.settings.USER_DICTIONARY_INSERT")) {
            String word = getIntent().getStringExtra(EXTRA_WORD);
            mAutoReturn = true;
            if (word != null) {
                showAddOrEditDialog(word);
            }
        }

        fillLangsSpinner();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if (mCursor != null)
    		mCursor.close();
    	if (mCurrentDictionary != null)
    		mCurrentDictionary.close();
    	
    	mCursor = null;
    	mCurrentDictionary = null;
    }

    void fillLangsSpinner() {
        new UserWordsEditorAsyncTask(this)
        {
            private ArrayList<String> mLangsList;

            @Override
            protected Void doInBackground(Void... params) {
                mLangsList = new ArrayList<String>();

                ArrayList<KeyboardAddOnAndBuilder> keyboards = KeyboardFactory
                        .getAllAvailableKeyboards(getApplicationContext());
                for (KeyboardAddOnAndBuilder kbd : keyboards)
                {
                    String locale = kbd.getKeyboardLocale();
                    if (TextUtils.isEmpty(locale))
                        continue;
                    if (mLangsList.contains(locale))
                        continue;
                    Log.d(TAG, "Adding locale " + locale + " to editor.");
                    mLangsList.add(locale);
                }
                return null;
            }

            @Override
            protected void applyResults(Void result) {
                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                        UserDictionaryEditorActivity.this, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                for (String lang : mLangsList)
                    adapter.add(lang);

                mLangs.setAdapter(adapter);
            };
        }.execute();
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mDialogEditingWord = state.getString(INSTANCE_KEY_DIALOG_EDITING_WORD);
        mAddedWordAlready = state.getBoolean(INSTANCE_KEY_ADDED_WORD, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INSTANCE_KEY_DIALOG_EDITING_WORD, mDialogEditingWord);
        outState.putBoolean(INSTANCE_KEY_ADDED_WORD, mAddedWordAlready);
    }

    private void showAddOrEditDialog(String editingWord) {
        mDialogEditingWord = editingWord;
        showDialog(DIALOG_ADD_OR_EDIT);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id)
        {
            case DIALOG_ADD_OR_EDIT:
                return createAddEditWordDialog();
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
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        return dialog;
    }

    public Dialog createAddEditWordDialog() {
        View content = getLayoutInflater().inflate(R.layout.dialog_edittext, null);
        final EditText editText = (EditText) content.findViewById(R.id.edittext);
        // No prediction in soft keyboard mode. TODO: Create a better way to
        // disable prediction
        editText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);

        return new AlertDialog.Builder(this)
                .setTitle(mDialogEditingWord != null
                        ? R.string.user_dict_settings_edit_dialog_title
                        : R.string.user_dict_settings_add_dialog_title)
                .setView(content)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onAddOrEditFinished(editText.getText().toString());
                        if (mAutoReturn)
                            finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mAutoReturn)
                            finish();
                    }
                })
                .create();
    }

    @Override
    protected void onPrepareDialog(int id, Dialog d) {
        switch (id)
        {
            case DIALOG_ADD_OR_EDIT:
                AlertDialog dialog = (AlertDialog) d;
                d.setTitle(mDialogEditingWord != null
                        ? R.string.user_dict_settings_edit_dialog_title
                        : R.string.user_dict_settings_add_dialog_title);
                EditText editText = (EditText) dialog.findViewById(R.id.edittext);
                editText.setText(mDialogEditingWord);
        }
    }

    private void onAddOrEditFinished(String word) {
        if (mDialogEditingWord != null) {
            // The user was editing a word, so do a delete/add
            deleteWord(mDialogEditingWord);
        }

        if (!TextUtils.isEmpty(word)) {
            // Disallow duplicates
            deleteWord(word);

            mCurrentDictionary.addWord(word, 128);
        }
        // mCursor.requery();
        fillWordsList();

        mAddedWordAlready = !TextUtils.isEmpty(word);
    }

    void deleteWord(String word) {
        mCurrentDictionary.deleteWord(word);
    }

    public void fillWordsList() {
        Log.d(TAG, "Selected locale is " + mSelectedLocale);
        new UserWordsEditorAsyncTask(this)
        {
        	private SafeUserDictionary mNewDictionary;
        	
        	@Override
        	protected void onPreExecute() {
        		super.onPreExecute();
        		//all the code below can be safely (and must) be called in the UI thread.
        		mNewDictionary = new SafeUserDictionary(getApplicationContext(), mSelectedLocale);
        		if (mNewDictionary != mCurrentDictionary && mCurrentDictionary != null) {
                	mCursor.close();
                    mCurrentDictionary.close();
                }
        	}
        	
            @Override
            protected Void doInBackground(Void... params) {
                try
                {
                    mCurrentDictionary = mNewDictionary;
                    mCurrentDictionary.loadDictionarySync();
                    mCursor = mCurrentDictionary.getWordsCursor();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void applyResults(Void result) {
                MyAdapter adapter = (MyAdapter) getListAdapter();
                if (AnyApplication.DEBUG)
            		Log.d(TAG, "Creating a new MyAdapter for the words editor");
                adapter = new MyAdapter();
                setListAdapter(adapter);
            };
        }.execute();
    }

    private class MyAdapter extends SimpleCursorAdapter /*
                                                         * implements
                                                         * SectionIndexer
                                                         * (removed because of
                                                         * issue 903)
                                                         */{
        // private AlphabetIndexer mIndexer;
        private final int mWordColumnIndex;

        public MyAdapter() {
            super(getApplicationContext(),
                    R.layout.user_dictionary_word_row,
                    mCursor.getCursor(),
                    new String[] {
                            UserDictionary.Words.WORD
                    },
                    new int[] {
                            android.R.id.text1
                    });

            mWordColumnIndex = mCursor.getCursor().getColumnIndexOrThrow(UserDictionary.Words.WORD);
            // String alphabet = getString(R.string.fast_scroll_alphabet);
            // mIndexer = new AlphabetIndexer(mCursor, mWordColumnIndex,
            // alphabet);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup v = (ViewGroup) super.getView(position, convertView, parent);
            final String word = ((Cursor) getItem(position)).getString(mWordColumnIndex);

            v.findViewById(R.id.edit_user_word).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    showAddOrEditDialog(word);
                }
            });

            v.findViewById(R.id.delete_user_word).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    deleteWord(word);

                    fillWordsList();
                }
            });

            return v;
        }
    }
}
