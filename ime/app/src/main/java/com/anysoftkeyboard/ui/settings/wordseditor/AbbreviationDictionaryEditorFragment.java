package com.anysoftkeyboard.ui.settings.wordseditor;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.anysoftkeyboard.dictionaries.EditableDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.AbbreviationsDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.WordsSQLiteConnectionPrefsProvider;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.anysoftkeyboard.prefs.backup.PrefsXmlStorage;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;
import java.util.List;
import net.evendanan.pixel.RxProgressDialog;

public class AbbreviationDictionaryEditorFragment extends UserDictionaryEditorFragment {

    private static final String ASK_ABBR_WORDS_SDCARD_FILENAME = "AbbrUserWords.xml";

    @NonNull private CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(
                this, getString(R.string.abbreviation_dict_settings_titlebar));
    }

    @Override
    public void onDestroy() {
        mDisposable.dispose();
        super.onDestroy();
    }

    @Override
    protected void restoreFromStorage() {
        // not calling base, since we have a different way of storing data
        mDisposable.dispose();
        mDisposable = new CompositeDisposable();

        PrefsXmlStorage storage =
                new PrefsXmlStorage(AnyApplication.getBackupFile(ASK_ABBR_WORDS_SDCARD_FILENAME));
        WordsSQLiteConnectionPrefsProvider provider =
                new WordsSQLiteConnectionPrefsProvider(
                        getContext(), AbbreviationsDictionary.ABBREVIATIONS_DB);

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
                                                UserDictionaryEditorFragment.DIALOG_LOAD_SUCCESS),
                                throwable ->
                                        mDialogController.showDialog(
                                                UserDictionaryEditorFragment.DIALOG_LOAD_FAILED,
                                                throwable.getMessage()),
                                this::fillWordsList));
    }

    @Override
    protected void backupToStorage() {
        // not calling base, since we have a different way of storing data
        mDisposable.dispose();
        mDisposable = new CompositeDisposable();

        PrefsXmlStorage storage =
                new PrefsXmlStorage(AnyApplication.getBackupFile(ASK_ABBR_WORDS_SDCARD_FILENAME));
        WordsSQLiteConnectionPrefsProvider provider =
                new WordsSQLiteConnectionPrefsProvider(
                        getContext(), AbbreviationsDictionary.ABBREVIATIONS_DB);

        mDisposable.add(
                RxProgressDialog.create(
                                Pair.create(storage, provider),
                                getActivity(),
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
                                                UserDictionaryEditorFragment.DIALOG_SAVE_SUCCESS),
                                throwable ->
                                        mDialogController.showDialog(
                                                UserDictionaryEditorFragment.DIALOG_SAVE_FAILED,
                                                throwable.getMessage()),
                                this::fillWordsList));
    }

    @Override
    protected EditableDictionary createEditableDictionary(String locale) {
        return new MyAbbreviationsDictionary(getActivity().getApplicationContext(), locale);
    }

    @Override
    protected EditorWordsAdapter createAdapterForWords(List<LoadedWord> wordsList) {
        Activity activity = getActivity();
        if (activity == null) return null;
        return new AbbreviationEditorWordsAdapter(wordsList, activity, this);
    }

    private static class AbbreviationEditorWordsAdapter extends EditorWordsAdapter {

        private final Context mContext;

        public AbbreviationEditorWordsAdapter(
                List<LoadedWord> editorWords,
                Context context,
                DictionaryCallbacks dictionaryCallbacks) {
            super(editorWords, LayoutInflater.from(context), dictionaryCallbacks);
            mContext = context;
        }

        @Override
        protected Editing createEmptyNewEditing() {
            return new Editing("", 0);
        }

        @Override
        protected void bindNormalWordViewText(TextView wordView, LoadedWord editorWord) {
            wordView.setText(
                    mContext.getString(
                            R.string.abbreviation_dict_word_template,
                            getAbbreviation(editorWord),
                            getExplodedSentence(editorWord)));
        }

        @Override
        protected View inflateEditingRowView(LayoutInflater layoutInflater, ViewGroup parent) {
            return layoutInflater.inflate(
                    R.layout.abbreviation_dictionary_word_row_edit, parent, false);
        }

        @Override
        protected void bindEditingWordViewText(EditText wordView, LoadedWord editorWord) {
            wordView.setText(getAbbreviation(editorWord));
            EditText explodedSentence =
                    ((View) wordView.getParent()).findViewById(R.id.word_target_view);
            explodedSentence.setText(getExplodedSentence(editorWord));
        }

        @Override
        protected LoadedWord createNewEditorWord(EditText wordView, LoadedWord oldEditorWord) {
            EditText explodedSentenceView =
                    ((View) wordView.getParent()).findViewById(R.id.word_target_view);
            final String newAbbreviation = wordView.getText().toString();
            final String newExplodedSentence = explodedSentenceView.getText().toString();
            if (TextUtils.isEmpty(newAbbreviation) || TextUtils.isEmpty(newExplodedSentence)) {
                return new LoadedWord(oldEditorWord.word, oldEditorWord.freq);
            } else {
                return new LoadedWord(
                        newAbbreviation + newExplodedSentence, newAbbreviation.length());
            }
        }

        private static String getAbbreviation(@Nullable LoadedWord word) {
            if (word == null) return "";
            return AbbreviationsDictionary.getAbbreviation(word.word, word.freq);
        }

        private static String getExplodedSentence(@Nullable LoadedWord word) {
            if (word == null) return "";
            return AbbreviationsDictionary.getExplodedSentence(word.word, word.freq);
        }
    }

    private static class MyAbbreviationsDictionary extends AbbreviationsDictionary
            implements MyEditableDictionary {

        @NonNull private List<LoadedWord> mLoadedWords = new ArrayList<>();

        public MyAbbreviationsDictionary(Context context, String locale) {
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
}
