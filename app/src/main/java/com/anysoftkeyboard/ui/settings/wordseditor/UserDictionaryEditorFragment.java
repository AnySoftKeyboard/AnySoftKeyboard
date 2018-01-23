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
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
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
import android.widget.Toast;

import com.anysoftkeyboard.PermissionsRequestCodes;
import com.anysoftkeyboard.dictionaries.EditableDictionary;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.dictionaries.content.AndroidUserDictionary;
import com.anysoftkeyboard.dictionaries.prefsprovider.UserDictionaryPrefsProvider;
import com.anysoftkeyboard.dictionaries.sqlite.FallbackUserDictionary;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.anysoftkeyboard.prefs.backup.PrefsXmlStorage;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.permissions.PermissionsRequest;
import net.evendanan.pushingpixels.RxProgressDialog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposables;

public class UserDictionaryEditorFragment extends Fragment implements EditorWordsAdapter.DictionaryCallbacks {

    static final int DIALOG_SAVE_SUCCESS = 10;
    static final int DIALOG_SAVE_FAILED = 11;
    static final int DIALOG_LOAD_SUCCESS = 20;
    static final int DIALOG_LOAD_FAILED = 21;
    static final String TAG = "ASK_UDE";
    private static final String ASK_USER_WORDS_SDCARD_FILENAME = "UserWords.xml";
    private static final Comparator<LoadedWord> msWordsComparator = (lhs, rhs) -> lhs.word.compareTo(rhs.word);
    private Dialog mDialog;
    private Spinner mLanguagesSpinner;

    @NonNull
    private CompositeDisposable mDisposable = new CompositeDisposable();

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

    @NonNull
    private File getBackupFile() {
        // http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
        final File externalFolder = Environment.getExternalStorageDirectory();
        return new File(new File(externalFolder, "/Android/data/" + getContext().getPackageName() + "/files/"), ASK_USER_WORDS_SDCARD_FILENAME);
    }

    private void restoreFromStorage() {
        mDisposable.dispose();
        mDisposable = new CompositeDisposable();

        PrefsXmlStorage storage = new PrefsXmlStorage(getBackupFile());
        UserDictionaryPrefsProvider provider = new UserDictionaryPrefsProvider(getContext());

        mDisposable.add(RxProgressDialog.create(Pair.create(storage, provider), getActivity())
                .subscribeOn(RxSchedulers.background())
                .map(pair -> {
                    final PrefsRoot prefsRoot = pair.first.load();
                    pair.second.storePrefsRoot(prefsRoot);
                    return Boolean.TRUE;
                })
                .observeOn(RxSchedulers.mainThread())
                .subscribe(
                        o -> showDialog(UserDictionaryEditorFragment.DIALOG_LOAD_SUCCESS),
                        throwable -> {
                            Toast.makeText(
                                    getContext().getApplicationContext(),
                                    getContext().getString(R.string.user_dict_restore_fail_text_with_error, throwable.getMessage()),
                                    Toast.LENGTH_LONG).show();
                            showDialog(UserDictionaryEditorFragment.DIALOG_LOAD_FAILED);
                        },
                        this::fillWordsList));
    }

    private void backupToStorage() {
        mDisposable.dispose();
        mDisposable = new CompositeDisposable();

        PrefsXmlStorage storage = new PrefsXmlStorage(getBackupFile());
        UserDictionaryPrefsProvider provider = new UserDictionaryPrefsProvider(getContext());

        mDisposable.add(RxProgressDialog.create(Pair.create(storage, provider), getActivity())
                .subscribeOn(RxSchedulers.background())
                .map(pair -> {
                    final PrefsRoot prefsRoot = pair.second.getPrefsRoot();
                    pair.first.store(prefsRoot);

                    return Boolean.TRUE;
                })
                .observeOn(RxSchedulers.mainThread())
                .subscribe(
                        o -> showDialog(UserDictionaryEditorFragment.DIALOG_SAVE_SUCCESS),
                        throwable -> {
                            Toast.makeText(
                                    getContext().getApplicationContext(),
                                    getContext().getString(R.string.user_dict_backup_fail_text_with_error, throwable.getMessage()),
                                    Toast.LENGTH_LONG).show();
                            showDialog(UserDictionaryEditorFragment.DIALOG_SAVE_FAILED);
                        },
                        this::fillWordsList));
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
        mDisposable.dispose();

        FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setCustomView(null);

        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
        mDialog = null;

        super.onDestroy();
    }

    private void fillLanguagesSpinner() {
        ArrayAdapter<DictionaryLocale> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Observable.fromIterable(AnyApplication.getKeyboardFactory(getContext()).getEnabledAddOns())
                .filter(kbd -> !TextUtils.isEmpty(kbd.getKeyboardLocale()))
                .map(kbd -> new DictionaryLocale(kbd.getKeyboardLocale(), kbd.getName()))
                .distinct()
                .blockingForEach(adapter::add);

        mLanguagesSpinner.setAdapter(adapter);
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
                throw new IllegalArgumentException("Failed to handle " + id + " in UserDictionaryEditorFragment#onCreateDialog");
        }
    }

    @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
    private Dialog createDialogAlert(int title, int text) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss()).create();
    }

    private void fillWordsList() {
        Logger.d(TAG, "Selected locale is %s", mSelectedLocale);
        mDisposable.dispose();
        mDisposable = new CompositeDisposable();
        final EditableDictionary editableDictionary = createEditableDictionary(mSelectedLocale);
        mDisposable.add(RxProgressDialog.create(editableDictionary, getActivity())
                .subscribeOn(RxSchedulers.background())
                .map(newDictionary -> {
                    newDictionary.loadDictionary();
                    List<LoadedWord> words = ((MyEditableDictionary) newDictionary).getLoadedWords();
                    //now, sorting the word list alphabetically
                    Collections.sort(words, msWordsComparator);

                    return Pair.create(newDictionary, words);
                })
                .observeOn(RxSchedulers.mainThread())
                .subscribe(pair -> {
                    final EditableDictionary newDictionary = pair.first;
                    mCurrentDictionary = newDictionary;
                    mDisposable.add(Disposables.fromAction(() -> {
                        newDictionary.close();
                        if (mCurrentDictionary == newDictionary) {
                            mCurrentDictionary = null;
                        }
                    }));
                    RecyclerView.Adapter adapter = createAdapterForWords(pair.second);
                    if (adapter != null) {
                        mWordsRecyclerView.setAdapter(adapter);
                    }
                })
        );
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
        mDisposable.add(RxProgressDialog.create(word, getActivity())
                .subscribeOn(RxSchedulers.background())
                .map(loadedWord -> {
                    deleteWord(loadedWord.word);
                    return Boolean.TRUE;
                })
                .observeOn(RxSchedulers.mainThread())
                .subscribe(aBoolean -> {
                }));
    }

    private void deleteWord(String word) {
        mCurrentDictionary.deleteWord(word);
    }

    @Override
    public void onWordUpdated(final String oldWord, final LoadedWord newWord) {
        mDisposable.add(RxProgressDialog.create(Pair.create(oldWord, newWord), getActivity())
                .subscribeOn(RxSchedulers.background())
                .map(pair -> {
                    //it can be empty in case it's a new word.
                    if (!TextUtils.isEmpty(pair.first))
                        deleteWord(pair.first);
                    deleteWord(pair.second.word);
                    mCurrentDictionary.addWord(pair.second.word, pair.second.freq);
                    return Boolean.TRUE;
                })
                .observeOn(RxSchedulers.mainThread())
                .subscribe(aBoolean -> {
                }));
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
            WordReadListener myListener = (word, frequency) -> {
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

    private static class MyAndroidUserDictionary extends AndroidUserDictionary implements MyEditableDictionary {

        @NonNull
        private List<LoadedWord> mLoadedWords = new ArrayList<>();

        public MyAndroidUserDictionary(Context context, String locale) {
            super(context, locale);
        }

        @Override
        protected void readWordsFromActualStorage(final WordReadListener listener) {
            mLoadedWords.clear();
            WordReadListener myListener = (word, frequency) -> {
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
}
