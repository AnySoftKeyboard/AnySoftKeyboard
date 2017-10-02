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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
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

import com.anysoftkeyboard.PermissionsRequestCodes;
import com.anysoftkeyboard.base.dictionaries.EditableDictionary;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.dictionaries.content.AndroidUserDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.FallbackUserDictionary;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.permissions.PermissionsRequest;
import net.evendanan.pushingpixels.AsyncTaskWithProgressWindow;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class UserDictionaryEditorFragment extends Fragment
        implements AsyncTaskWithProgressWindow.AsyncTaskOwner, EditorWordsAdapter.DictionaryCallbacks {

    static final int DIALOG_SAVE_SUCCESS = 10;
    static final int DIALOG_SAVE_FAILED = 11;
    static final int DIALOG_LOAD_SUCCESS = 20;
    static final int DIALOG_LOAD_FAILED = 21;
    static final String TAG = "ASK_UDE";
    private static final String ASK_USER_WORDS_SDCARD_FILENAME = "UserWords.xml";
    private static final Comparator<LoadedWord> msWordsComparator = new Comparator<LoadedWord>() {
        @Override
        public int compare(LoadedWord lhs, LoadedWord rhs) {
            return lhs.word.compareTo(rhs.word);
        }
    };
    private Dialog mDialog;
    private Spinner mLanguagesSpinner;

    private String mSelectedLocale = null;
    private EditableDictionary mCurrentDictionary;

    private RecyclerView mWordsRecyclerView;
    private final OnItemSelectedListener mSpinnerItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mSelectedLocale = ((DictionaryLocale) arg0.getItemAtPosition(arg2)).getLocale();
            fillWordsList();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            Logger.d(TAG, "No locale selected");
            mSelectedLocale = null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.words_editor_actionbar_view, null);
        mLanguagesSpinner = (Spinner) v.findViewById(R.id.user_dictionay_langs);
        actionBar.setCustomView(v);

        return inflater.inflate(R.layout.user_dictionary_editor, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLanguagesSpinner.setOnItemSelectedListener(mSpinnerItemSelectedListener);

        mWordsRecyclerView = (RecyclerView) view.findViewById(R.id.words_recycler_view);
        mWordsRecyclerView.setHasFixedSize(false);
        final int wordsEditorColumns = getResources().getInteger(R.integer.words_editor_columns_count);
        if (wordsEditorColumns > 1) {
            mWordsRecyclerView.addItemDecoration(new MarginDecoration(getActivity()));
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
        MainSettingsActivity mainSettingsActivity = (MainSettingsActivity) getActivity();
        if (mainSettingsActivity == null) return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.add_user_word:
                createEmptyItemForAdd();
                return true;
            case R.id.backup_words:
                //we required Storage permission
                mainSettingsActivity.startPermissionsRequest(new StoragePermissionRequest(this, false));
                return true;
            case R.id.restore_words:
                mainSettingsActivity.startPermissionsRequest(new StoragePermissionRequest(this, true));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void restoreFromStorage() {
        new RestoreUserWordsAsyncTask(UserDictionaryEditorFragment.this, ASK_USER_WORDS_SDCARD_FILENAME).execute();
    }

    private void backupToStorage() {
        new BackupUserWordsAsyncTask(UserDictionaryEditorFragment.this, ASK_USER_WORDS_SDCARD_FILENAME).execute();
    }

    private void createEmptyItemForAdd() {
        EditorWordsAdapter adapter = (EditorWordsAdapter) mWordsRecyclerView.getAdapter();
        if (adapter == null || !isResumed()) return;
        adapter.addNewWordAtEnd(mWordsRecyclerView);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.user_dict_settings_titlebar));
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
        if (mCurrentDictionary != null)
            mCurrentDictionary.close();

        mCurrentDictionary = null;
    }

    void fillLanguagesSpinner() {
        new FillSpinnerWordsEditorAsyncTask(this).execute();
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
            default:
                throw new IllegalArgumentException("Failed to handle "+id+" in UserDictionaryEditorFragment#onCreateDialog");
        }
    }

    @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
    private Dialog createDialogAlert(int title, int text) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }

    private void fillWordsList() {
        Logger.d(TAG, "Selected locale is " + mSelectedLocale);
        new FillWordsEditorAsyncTask(this).execute();
    }

    protected EditorWordsAdapter createAdapterForWords(List<LoadedWord> wordsList) {
        Activity activity = getActivity();
        if (activity == null) return null;
        return new EditorWordsAdapter(wordsList, LayoutInflater.from(activity), this);
    }

    /*package*/Spinner getLanguagesSpinner() {
        return mLanguagesSpinner;
    }

    @VisibleForTesting
    /*package*/OnItemSelectedListener getSpinnerItemSelectedListener() {
        return mSpinnerItemSelectedListener;
    }

    protected EditableDictionary createEditableDictionary(String locale) {
        return new MyUserDictionary(getActivity().getApplicationContext(), locale);
    }

    @Override
    public void onWordDeleted(final LoadedWord word) {
        new DeleteUserWordsEditorAsyncTask(this, word).execute();
    }

    private void deleteWord(String word) {
        mCurrentDictionary.deleteWord(word);
    }

    @Override
    public void onWordUpdated(final String oldWord, final LoadedWord newWord) {
        new AddWordUserWordsEditorAsyncTask(this, oldWord, newWord).execute();
    }

    protected interface MyEditableDictionary {
        @NonNull
        List<LoadedWord> getLoadedWords();
    }

    public static class LoadedWord {
        public final String word;
        public final int freq;

        public LoadedWord(String word, int freq) {
            this.word = word;
            this.freq = freq;
        }
    }

    private static class MarginDecoration extends RecyclerView.ItemDecoration {
        private final int mMargin;

        public MarginDecoration(Context context) {
            mMargin = context.getResources().getDimensionPixelSize(R.dimen.global_content_padding_side);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(mMargin, mMargin, mMargin, mMargin);
        }
    }

    private static class StoragePermissionRequest extends PermissionsRequest.PermissionsRequestBase {

        private final WeakReference<UserDictionaryEditorFragment> mFragmentWeakReference;
        private final boolean mForRead;

        public StoragePermissionRequest(UserDictionaryEditorFragment fragment, boolean forRead) {
            super(PermissionsRequestCodes.STORAGE.getRequestCode(),
                    getPermissionsForOsVersion());
            mForRead = forRead;
            mFragmentWeakReference = new WeakReference<>(fragment);
        }

        @NonNull
        private static String[] getPermissionsForOsVersion() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            } else {
                return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
        }

        @Override
        public void onPermissionsGranted() {
            UserDictionaryEditorFragment fragment = mFragmentWeakReference.get();
            if (fragment == null) return;

            if (mForRead)
                fragment.restoreFromStorage();
            else
                fragment.backupToStorage();
        }

        @Override
        public void onPermissionsDenied(@NonNull String[] grantedPermissions, @NonNull String[] deniedPermissions, @NonNull String[] declinedPermissions) {
            /*no-op - Main-Activity handles this case*/
        }
    }

    private static class MyUserDictionary extends UserDictionary implements MyEditableDictionary {

        public MyUserDictionary(Context context, String locale) {
            super(context, locale);
        }

        @NonNull
        @Override
        public List<LoadedWord> getLoadedWords() {
            return ((MyEditableDictionary) super.getActualDictionary()).getLoadedWords();
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

    private static class MyFallbackUserDictionary extends FallbackUserDictionary implements MyEditableDictionary {

        @NonNull
        private List<LoadedWord> mLoadedWords = new ArrayList<>();

        public MyFallbackUserDictionary(Context context, String locale) {
            super(context, locale);
        }

        @Override
        protected void readWordsFromActualStorage(final WordReadListener listener) {
            mLoadedWords.clear();
            WordReadListener myListener = new WordReadListener() {
                @Override
                public boolean onWordRead(String word, int frequency) {
                    mLoadedWords.add(new LoadedWord(word, frequency));
                    return listener.onWordRead(word, frequency);
                }
            };
            super.readWordsFromActualStorage(myListener);
        }

        @NonNull
        @Override
        public List<LoadedWord> getLoadedWords() {
            return mLoadedWords;
        }
    }

    private static class MyAndroidUserDictionary extends AndroidUserDictionary implements MyEditableDictionary {

        @NonNull
        private List<LoadedWord> mLoadedWords = new ArrayList<>();

        public MyAndroidUserDictionary(Context context, String locale) {
            super(context, locale);
        }

        @Override
        protected void readWordsFromActualStorage(final WordReadListener listener) {
            mLoadedWords.clear();
            WordReadListener myListener = new WordReadListener() {
                @Override
                public boolean onWordRead(String word, int frequency) {
                    mLoadedWords.add(new LoadedWord(word, frequency));
                    return listener.onWordRead(word, frequency);
                }
            };
            super.readWordsFromActualStorage(myListener);
        }

        @NonNull
        @Override
        public List<LoadedWord> getLoadedWords() {
            return mLoadedWords;
        }
    }

    private static class AddWordUserWordsEditorAsyncTask extends UserWordsEditorAsyncTask {
        private final String mOldWord;
        private final LoadedWord mNewWord;

        public AddWordUserWordsEditorAsyncTask(UserDictionaryEditorFragment owner, String oldWord, LoadedWord newWord) {
            super(owner, false);
            mOldWord = oldWord;
            mNewWord = newWord;
        }

        @Override
        protected Void doAsyncTask(Void[] params) throws Exception {
            final UserDictionaryEditorFragment owner = getOwner();
            if (owner == null) return null;

            if (!TextUtils.isEmpty(mOldWord))//it can be empty in case it's a new word.
                owner.deleteWord(mOldWord);
            owner.deleteWord(mNewWord.word);
            owner.mCurrentDictionary.addWord(mNewWord.word, mNewWord.freq);
            return null;
        }

        @Override
        protected void applyResults(Void v, Exception backgroundException) {
        }
    }

    private static class DeleteUserWordsEditorAsyncTask extends UserWordsEditorAsyncTask {
        private final LoadedWord mWord;

        public DeleteUserWordsEditorAsyncTask(UserDictionaryEditorFragment owner, LoadedWord word) {
            super(owner, false);
            this.mWord = word;
        }

        @Override
        protected Void doAsyncTask(Void[] params) throws Exception {
            final UserDictionaryEditorFragment owner = getOwner();
            if (owner == null) return null;
            owner.deleteWord(mWord.word);
            return null;
        }

        @Override
        protected void applyResults(Void v, Exception backgroundException) {
        }
    }

    private static class FillWordsEditorAsyncTask extends UserWordsEditorAsyncTask {
        private EditableDictionary mNewDictionary;
        private List<LoadedWord> mWordsList;

        public FillWordsEditorAsyncTask(UserDictionaryEditorFragment owner) {
            super(owner, true);

            mNewDictionary = owner.createEditableDictionary(owner.mSelectedLocale);
            if (mNewDictionary != owner.mCurrentDictionary && owner.mCurrentDictionary != null) {
                owner.mCurrentDictionary.close();
            }
        }

        @Override
        protected Void doAsyncTask(Void[] params) throws Exception {
            final UserDictionaryEditorFragment owner = getOwner();
            if (owner == null) return null;

            owner.mCurrentDictionary = mNewDictionary;
            owner.mCurrentDictionary.loadDictionary();
            mWordsList = ((MyEditableDictionary) owner.mCurrentDictionary).getLoadedWords();
            //now, sorting the word list alphabetically
            Collections.sort(mWordsList, msWordsComparator);
            return null;
        }

        @Override
        protected void applyResults(Void result, Exception backgroundException) {
            final UserDictionaryEditorFragment owner = getOwner();
            if (owner == null) return;

            RecyclerView.Adapter adapter = owner.createAdapterForWords(mWordsList);
            if (adapter != null) {
                owner.mWordsRecyclerView.setAdapter(adapter);
            }
        }
    }

    private static class FillSpinnerWordsEditorAsyncTask extends UserWordsEditorAsyncTask {
        private final ArrayAdapter<DictionaryLocale> mAdapter;

        public FillSpinnerWordsEditorAsyncTask(UserDictionaryEditorFragment owner) {
            super(owner, true);
            mAdapter = new ArrayAdapter<>(owner.getActivity(), android.R.layout.simple_spinner_item);
        }

        @Override
        protected Void doAsyncTask(Void[] params) throws Exception {
            final UserDictionaryEditorFragment owner = getOwner();
            if (owner == null) return null;

            ArrayList<DictionaryLocale> languagesList = new ArrayList<>();

            List<KeyboardAddOnAndBuilder> keyboards = AnyApplication.getKeyboardFactory(owner.getContext()).getEnabledAddOns();
            for (KeyboardAddOnAndBuilder kbd : keyboards) {
                String locale = kbd.getKeyboardLocale();
                if (TextUtils.isEmpty(locale))
                    continue;

                DictionaryLocale dictionaryLocale = new DictionaryLocale(locale, kbd.getName());
                //Don't worry, DictionaryLocale equals any DictionaryLocale with the same locale (no matter what its name is)
                if (languagesList.contains(dictionaryLocale))
                    continue;
                Logger.d(TAG, "Adding locale " + locale + " to editor.");
                languagesList.add(dictionaryLocale);
            }

            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for (DictionaryLocale lang : languagesList)
                mAdapter.add(lang);

            return null;
        }

        @Override
        protected void applyResults(Void result, Exception backgroundException) {
            final UserDictionaryEditorFragment owner = getOwner();
            if (owner == null) return;

            owner.mLanguagesSpinner.setAdapter(mAdapter);
        }
    }
}
