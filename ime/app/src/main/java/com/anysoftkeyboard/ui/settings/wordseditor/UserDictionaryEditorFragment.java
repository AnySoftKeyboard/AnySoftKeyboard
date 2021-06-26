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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBar;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.anysoftkeyboard.android.PermissionRequestHelper;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.EditableDictionary;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.dictionaries.content.AndroidUserDictionary;
import com.anysoftkeyboard.dictionaries.prefsprovider.UserDictionaryPrefsProvider;
import com.anysoftkeyboard.dictionaries.sqlite.FallbackUserDictionary;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.anysoftkeyboard.prefs.backup.PrefsXmlStorage;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.pixel.GeneralDialogController;
import net.evendanan.pixel.RxProgressDialog;
import pub.devrel.easypermissions.AfterPermissionGranted;

public class UserDictionaryEditorFragment extends Fragment
        implements EditorWordsAdapter.DictionaryCallbacks {

    static final int DIALOG_SAVE_SUCCESS = 10;
    static final int DIALOG_SAVE_FAILED = 11;
    static final int DIALOG_LOAD_SUCCESS = 20;
    static final int DIALOG_LOAD_FAILED = 21;
    static final String TAG = "ASK_UDE";
    private static final String ASK_USER_WORDS_SDCARD_FILENAME = "UserWords.xml";
    private static final Comparator<LoadedWord> msWordsComparator =
            (lhs, rhs) -> lhs.word.compareTo(rhs.word);
    protected GeneralDialogController mDialogController;
    private Spinner mLanguagesSpinner;

    @NonNull private CompositeDisposable mDisposable = new CompositeDisposable();

    private String mSelectedLocale = null;
    private EditableDictionary mCurrentDictionary;

    private RecyclerView mWordsRecyclerView;
    private final OnItemSelectedListener mSpinnerItemSelectedListener =
            new OnItemSelectedListener() {
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialogController =
                new GeneralDialogController(
                        getActivity(),
                        (builder, optionId, data) -> {
                            builder.setPositiveButton(android.R.string.ok, null);
                            switch (optionId) {
                                case DIALOG_SAVE_SUCCESS:
                                    builder.setTitle(R.string.user_dict_backup_success_title);
                                    builder.setMessage(R.string.user_dict_backup_success_text);
                                    break;
                                case DIALOG_SAVE_FAILED:
                                    builder.setTitle(R.string.user_dict_backup_fail_title);
                                    builder.setMessage(
                                            getString(
                                                    R.string.user_dict_backup_fail_text_with_error,
                                                    data));
                                    break;
                                case DIALOG_LOAD_SUCCESS:
                                    builder.setTitle(R.string.user_dict_restore_success_title);
                                    builder.setMessage(R.string.user_dict_restore_success_text);
                                    break;
                                case DIALOG_LOAD_FAILED:
                                    builder.setTitle(R.string.user_dict_restore_fail_title);
                                    builder.setMessage(
                                            getString(
                                                    R.string.user_dict_restore_fail_text_with_error,
                                                    data));
                                    break;
                                default:
                                    throw new IllegalArgumentException(
                                            "Failed to handle "
                                                    + optionId
                                                    + " in UserDictionaryEditorFragment#onCreateDialog");
                            }
                        });
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.words_editor_actionbar_view, null);
        mLanguagesSpinner = v.findViewById(R.id.user_dictionay_langs);
        actionBar.setCustomView(v);

        return inflater.inflate(R.layout.user_dictionary_editor, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLanguagesSpinner.setOnItemSelectedListener(mSpinnerItemSelectedListener);

        mWordsRecyclerView = view.findViewById(R.id.words_recycler_view);
        mWordsRecyclerView.setHasFixedSize(false);
        final int wordsEditorColumns =
                getResources().getInteger(R.integer.words_editor_columns_count);
        if (wordsEditorColumns > 1) {
            mWordsRecyclerView.addItemDecoration(new MarginDecoration(getActivity()));
            mWordsRecyclerView.setLayoutManager(
                    new GridLayoutManager(getActivity(), wordsEditorColumns));
        } else {
            mWordsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.words_editor_menu_actions, menu);
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
                backupToStorage();
                return true;
            case R.id.restore_words:
                restoreFromStorage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @AfterPermissionGranted(PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_READ_CODE)
    protected void restoreFromStorage() {
        mDisposable.dispose();
        mDisposable = new CompositeDisposable();

        if (PermissionRequestHelper.check(
                this, PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_READ_CODE)) {
            PrefsXmlStorage storage =
                    new PrefsXmlStorage(
                            AnyApplication.getBackupFile(
                                    requireContext(), ASK_USER_WORDS_SDCARD_FILENAME));
            UserDictionaryPrefsProvider provider = new UserDictionaryPrefsProvider(getContext());

            mDisposable.add(
                    RxProgressDialog.create(
                                    Pair.create(storage, provider),
                                    getActivity(),
                                    R.layout.progress_window)
                            .subscribeOn(RxSchedulers.background())
                            .map(
                                    pair -> {
                                        final PrefsRoot prefsRoot = pair.first.load();
                                        pair.second.storePrefsRoot(prefsRoot);
                                        return Boolean.TRUE;
                                    })
                            .observeOn(RxSchedulers.mainThread())
                            .subscribe(
                                    o ->
                                            mDialogController.showDialog(
                                                    UserDictionaryEditorFragment
                                                            .DIALOG_LOAD_SUCCESS),
                                    throwable ->
                                            mDialogController.showDialog(
                                                    UserDictionaryEditorFragment.DIALOG_LOAD_FAILED,
                                                    throwable.getMessage()),
                                    this::fillWordsList));
        }
    }

    @AfterPermissionGranted(PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_WRITE_CODE)
    protected void backupToStorage() {
        mDisposable.dispose();
        mDisposable = new CompositeDisposable();

        if (PermissionRequestHelper.check(
                this, PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_WRITE_CODE)) {
            PrefsXmlStorage storage =
                    new PrefsXmlStorage(
                            AnyApplication.getBackupFile(
                                    requireContext(), ASK_USER_WORDS_SDCARD_FILENAME));
            UserDictionaryPrefsProvider provider = new UserDictionaryPrefsProvider(getContext());

            mDisposable.add(
                    RxProgressDialog.create(
                                    Pair.create(storage, provider),
                                    requireActivity(),
                                    R.layout.progress_window)
                            .subscribeOn(RxSchedulers.background())
                            .map(
                                    pair -> {
                                        final PrefsRoot prefsRoot = pair.second.getPrefsRoot();
                                        pair.first.store(prefsRoot);

                                        return Boolean.TRUE;
                                    })
                            .observeOn(RxSchedulers.mainThread())
                            .subscribe(
                                    o ->
                                            mDialogController.showDialog(
                                                    UserDictionaryEditorFragment
                                                            .DIALOG_SAVE_SUCCESS),
                                    throwable ->
                                            mDialogController.showDialog(
                                                    UserDictionaryEditorFragment.DIALOG_SAVE_FAILED,
                                                    throwable.getMessage()),
                                    this::fillWordsList));
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
        MainSettingsActivity.setActivityTitle(
                this, getString(R.string.user_dict_settings_titlebar));

        fillLanguagesSpinner();
    }

    @Override
    public void onDestroy() {
        mDisposable.dispose();

        FragmentChauffeurActivity activity = (FragmentChauffeurActivity) requireActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setCustomView(null);

        mDialogController.dismiss();

        super.onDestroy();
    }

    private void fillLanguagesSpinner() {
        ArrayAdapter<DictionaryLocale> adapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Observable.fromIterable(
                        AnyApplication.getKeyboardFactory(requireContext()).getEnabledAddOns())
                .filter(kbd -> !TextUtils.isEmpty(kbd.getKeyboardLocale()))
                .map(kbd -> new DictionaryLocale(kbd.getKeyboardLocale(), kbd.getName()))
                .distinct()
                .blockingForEach(adapter::add);

        mLanguagesSpinner.setAdapter(adapter);
    }

    protected void fillWordsList() {
        Logger.d(TAG, "Selected locale is %s", mSelectedLocale);
        mDisposable.dispose();
        mDisposable = new CompositeDisposable();
        final EditableDictionary editableDictionary = createEditableDictionary(mSelectedLocale);
        mDisposable.add(
                RxProgressDialog.create(
                                editableDictionary, requireActivity(), R.layout.progress_window)
                        .subscribeOn(RxSchedulers.background())
                        .map(
                                newDictionary -> {
                                    newDictionary.loadDictionary();
                                    List<LoadedWord> words =
                                            ((MyEditableDictionary) newDictionary).getLoadedWords();
                                    // now, sorting the word list alphabetically
                                    Collections.sort(words, msWordsComparator);

                                    return Pair.create(newDictionary, words);
                                })
                        .observeOn(RxSchedulers.mainThread())
                        .subscribe(
                                pair -> {
                                    final EditableDictionary newDictionary = pair.first;
                                    mCurrentDictionary = newDictionary;
                                    mDisposable.add(
                                            Disposables.fromAction(
                                                    () -> {
                                                        newDictionary.close();
                                                        if (mCurrentDictionary == newDictionary) {
                                                            mCurrentDictionary = null;
                                                        }
                                                    }));
                                    EditorWordsAdapter adapter = createAdapterForWords(pair.second);
                                    if (adapter != null) {
                                        mWordsRecyclerView.setAdapter(adapter);
                                    }
                                },
                                GenericOnError.onError(
                                        "Failed to load words from dictionary for editor.")));
    }

    protected EditorWordsAdapter createAdapterForWords(List<LoadedWord> wordsList) {
        Activity activity = getActivity();
        if (activity == null) return null;
        return new EditorWordsAdapter(wordsList, LayoutInflater.from(activity), this);
    }

    /*package*/ Spinner getLanguagesSpinner() {
        return mLanguagesSpinner;
    }

    @VisibleForTesting
    /*package*/ OnItemSelectedListener getSpinnerItemSelectedListener() {
        return mSpinnerItemSelectedListener;
    }

    protected EditableDictionary createEditableDictionary(String locale) {
        return new MyUserDictionary(requireContext().getApplicationContext(), locale);
    }

    @Override
    public void onWordDeleted(final LoadedWord word) {
        mDisposable.add(
                RxProgressDialog.create(word, requireActivity(), R.layout.progress_window)
                        .subscribeOn(RxSchedulers.background())
                        .map(
                                loadedWord -> {
                                    deleteWord(loadedWord.word);
                                    return Boolean.TRUE;
                                })
                        .observeOn(RxSchedulers.mainThread())
                        .subscribe(aBoolean -> {}));
    }

    private void deleteWord(String word) {
        mCurrentDictionary.deleteWord(word);
    }

    @Override
    public void onWordUpdated(final String oldWord, final LoadedWord newWord) {
        mDisposable.add(
                RxProgressDialog.create(
                                Pair.create(oldWord, newWord),
                                requireActivity(),
                                R.layout.progress_window)
                        .subscribeOn(RxSchedulers.background())
                        .map(
                                pair -> {
                                    // it can be empty in case it's a new word.
                                    if (!TextUtils.isEmpty(pair.first)) {
                                        deleteWord(pair.first);
                                    }
                                    deleteWord(pair.second.word);
                                    mCurrentDictionary.addWord(pair.second.word, pair.second.freq);
                                    return Boolean.TRUE;
                                })
                        .observeOn(RxSchedulers.mainThread())
                        .subscribe(aBoolean -> {}));
    }

    protected interface MyEditableDictionary {
        @NonNull
        List<LoadedWord> getLoadedWords();
    }

    public static class LoadedWord {
        public final String word;
        public final int freq;

        LoadedWord(String word, int freq) {
            this.word = word;
            this.freq = freq;
        }
    }

    private static class MarginDecoration extends RecyclerView.ItemDecoration {
        private final int mMargin;

        MarginDecoration(Context context) {
            mMargin =
                    context.getResources()
                            .getDimensionPixelSize(R.dimen.global_content_padding_side);
        }

        @Override
        public void getItemOffsets(
                Rect outRect,
                @NonNull View view,
                @NonNull RecyclerView parent,
                @NonNull RecyclerView.State state) {
            outRect.set(mMargin, mMargin, mMargin, mMargin);
        }
    }

    private static class MyUserDictionary extends UserDictionary implements MyEditableDictionary {

        MyUserDictionary(Context context, String locale) {
            super(context, locale);
        }

        @NonNull
        @Override
        public List<LoadedWord> getLoadedWords() {
            return ((MyEditableDictionary) super.getActualDictionary()).getLoadedWords();
        }

        @NonNull
        @Override
        protected AndroidUserDictionary createAndroidUserDictionary(
                Context context, String locale) {
            return new MyAndroidUserDictionary(context, locale);
        }

        @NonNull
        @Override
        protected FallbackUserDictionary createFallbackUserDictionary(
                Context context, String locale) {
            return new MyFallbackUserDictionary(context, locale);
        }
    }

    private static class MyFallbackUserDictionary extends FallbackUserDictionary
            implements MyEditableDictionary {

        @NonNull private final List<LoadedWord> mLoadedWords = new ArrayList<>();

        MyFallbackUserDictionary(Context context, String locale) {
            super(context, locale);
        }

        @Override
        protected void readWordsFromActualStorage(final WordReadListener listener) {
            mLoadedWords.clear();
            WordReadListener myListener =
                    (word, frequency) -> {
                        mLoadedWords.add(new LoadedWord(word, frequency));
                        return listener.onWordRead(word, frequency);
                    };
            super.readWordsFromActualStorage(myListener);
        }

        @NonNull
        @Override
        public List<LoadedWord> getLoadedWords() {
            return mLoadedWords;
        }
    }

    private static class MyAndroidUserDictionary extends AndroidUserDictionary
            implements MyEditableDictionary {

        @NonNull private final List<LoadedWord> mLoadedWords = new ArrayList<>();

        MyAndroidUserDictionary(Context context, String locale) {
            super(context, locale);
        }

        @Override
        protected void readWordsFromActualStorage(final WordReadListener listener) {
            mLoadedWords.clear();
            WordReadListener myListener =
                    (word, frequency) -> {
                        mLoadedWords.add(new LoadedWord(word, frequency));
                        return listener.onWordRead(word, frequency);
                    };
            super.readWordsFromActualStorage(myListener);
        }

        @NonNull
        @Override
        public List<LoadedWord> getLoadedWords() {
            return mLoadedWords;
        }
    }

    @SuppressWarnings("deprecation") // required for permissions flow
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionRequestHelper.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }
}
