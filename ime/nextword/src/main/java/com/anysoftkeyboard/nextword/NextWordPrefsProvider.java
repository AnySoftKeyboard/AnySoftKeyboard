package com.anysoftkeyboard.nextword;

import android.content.Context;
import com.anysoftkeyboard.prefs.backup.PrefItem;
import com.anysoftkeyboard.prefs.backup.PrefsProvider;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import java.util.ArrayList;
import java.util.List;

public class NextWordPrefsProvider implements PrefsProvider {
    private final Context mContext;
    private final Iterable<String> mLocaleToStore;

    public NextWordPrefsProvider(Context context, Iterable<String> localeToStore) {
        mContext = context;
        mLocaleToStore = localeToStore;
    }

    @Override
    public String providerId() {
        return "NextWordPrefsProvider";
    }

    @Override
    public PrefsRoot getPrefsRoot() {
        final PrefsRoot root = new PrefsRoot(1);

        for (String locale : mLocaleToStore) {
            final PrefItem localeChild = root.createChild().addValue("locale", locale);

            NextWordsStorage storage = new NextWordsStorage(mContext, locale);
            for (NextWordsContainer nextWordsContainer : storage.loadStoredNextWords()) {
                final PrefItem word =
                        localeChild
                                .createChild()
                                .addValue("word", nextWordsContainer.word.toString());

                for (NextWord nextWord : nextWordsContainer.getNextWordSuggestions()) {
                    word.createChild()
                            .addValue("nextWord", nextWord.nextWord)
                            .addValue("usedCount", Integer.toString(nextWord.getUsedCount()));
                }
            }
        }

        return root;
    }

    @Override
    public void storePrefsRoot(PrefsRoot prefsRoot) {
        for (PrefItem localePref : prefsRoot.getChildren()) {
            final String locale = localePref.getValue("locale");
            if (locale == null) continue;

            List<NextWordsContainer> wordsToStore = new ArrayList<>();
            for (PrefItem word : localePref.getChildren()) {
                NextWordsContainer container = new NextWordsContainer(word.getValue("word"));
                for (PrefItem nextWord : word.getChildren()) {
                    container.markWordAsUsed(nextWord.getValue("nextWord"));
                }
                wordsToStore.add(container);
            }

            NextWordsStorage storage = new NextWordsStorage(mContext, locale);
            storage.storeNextWords(wordsToStore);
        }
    }
}
