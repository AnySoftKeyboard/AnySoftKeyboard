package com.anysoftkeyboard.dictionaries.prefsprovider;

import android.content.Context;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.prefs.backup.PrefItem;
import com.anysoftkeyboard.prefs.backup.PrefsProvider;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.menny.android.anysoftkeyboard.AnyApplication;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

public class UserDictionaryPrefsProvider implements PrefsProvider {
    private final Context mContext;
    private final Iterable<String> mLocaleToStore;

    public UserDictionaryPrefsProvider(Context context) {
        mContext = context;
        mLocaleToStore = Observable.fromIterable(AnyApplication.getExternalDictionaryFactory(mContext).getAllAddOns())
                .map(DictionaryAddOnAndBuilder::getLanguage).distinct().blockingIterable();
    }

    public UserDictionaryPrefsProvider(Context context, List<String> localeToStore) {
        mContext = context;
        mLocaleToStore = Collections.unmodifiableCollection(localeToStore);
    }

    @Override
    public String providerId() {
        return "UserDictionaryPrefsProvider";
    }

    @Override
    public PrefsRoot getPrefsRoot() {
        final PrefsRoot root = new PrefsRoot(1);

        for (String locale : mLocaleToStore) {
            final PrefItem localeChild = root.createChild();
            localeChild.addValue("locale", locale);

            TappingUserDictionary dictionary = new TappingUserDictionary(mContext, locale,
                    (word, frequency) -> {
                        localeChild.createChild()
                                .addValue("word", word)
                                .addValue("freq", Integer.toString(frequency));

                        return true;
                    });

            dictionary.loadDictionary();

            dictionary.close();
        }

        return root;
    }

    @Override
    public void storePrefsRoot(PrefsRoot prefsRoot) throws Exception {

        Observable.fromIterable(prefsRoot.getChildren()).blockingForEach(prefItem -> {
            final String locale = prefItem.getValue("locale");
            if (TextUtils.isEmpty(locale)) return;

            final UserDictionary userDictionary = new UserDictionary(mContext, locale);
            userDictionary.loadDictionary();

            Observable.fromIterable(prefItem.getChildren())
                    .map(prefItem1 -> Pair.create(prefItem1.getValue("word"), Integer.parseInt(prefItem1.getValue("freq"))))
                    .blockingSubscribe(
                            word -> userDictionary.addWord(word.first, word.second),
                            throwable -> {
                            },
                            userDictionary::close);
        });
    }
}
