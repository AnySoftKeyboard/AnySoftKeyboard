package com.anysoftkeyboard.dictionaries.sqlite;

import android.content.Context;
import android.support.v4.util.Pair;
import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.prefs.backup.PrefsProvider;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.menny.android.anysoftkeyboard.AnyApplication;
import io.reactivex.Observable;

public class WordsSQLiteConnectionPrefsProvider implements PrefsProvider {

    private final Context mContext;
    private final String mDatabaseFilename;
    private final Iterable<String> mLocale;

    public WordsSQLiteConnectionPrefsProvider(Context context, String databaseFilename) {
        this(
                context,
                databaseFilename,
                Observable.fromIterable(
                                AnyApplication.getExternalDictionaryFactory(context).getAllAddOns())
                        .map(DictionaryAddOnAndBuilder::getLanguage)
                        .distinct()
                        .blockingIterable());
    }

    public WordsSQLiteConnectionPrefsProvider(
            Context context, String databaseFilename, Iterable<String> locale) {
        mContext = context;
        mDatabaseFilename = databaseFilename;
        mLocale = locale;
    }

    @Override
    public String providerId() {
        return "WordsSQLiteConnectionPrefsProvider";
    }

    @Override
    public PrefsRoot getPrefsRoot() {
        final PrefsRoot root = new PrefsRoot(1);

        Observable.fromIterable(mLocale)
                .map(
                        locale ->
                                new Pair<>(
                                        root.createChild().addValue("locale", locale),
                                        new WordsSQLiteConnection(
                                                mContext, mDatabaseFilename, locale)))
                .blockingSubscribe(
                        pair ->
                                pair.second.loadWords(
                                        (word, frequency) -> {
                                            pair.first
                                                    .createChild()
                                                    .addValue("word", word)
                                                    .addValue("freq", Integer.toString(frequency));
                                            return true;
                                        }));

        return root;
    }

    @Override
    public void storePrefsRoot(PrefsRoot prefsRoot) {
        Observable.fromIterable(prefsRoot.getChildren())
                .map(
                        prefItem ->
                                new Pair<>(
                                        new WordsSQLiteConnection(
                                                mContext,
                                                mDatabaseFilename,
                                                prefItem.getValue("locale")),
                                        Observable.fromIterable(prefItem.getChildren())))
                .blockingSubscribe(
                        pair ->
                                pair.second.blockingSubscribe(
                                        prefItem -> {
                                            pair.first.addWord(
                                                    prefItem.getValue("word"),
                                                    Integer.parseInt(prefItem.getValue("freq")));
                                        }));
    }
}
