package com.anysoftkeyboard.nextword;

import android.support.annotation.NonNull;

public interface NextWordSuggestions {
    @NonNull
    Iterable<String> getNextWords(@NonNull CharSequence currentWord, int maxResults, int minWordUsage);

    void notifyNextTypedWord(@NonNull CharSequence currentWord);

    void resetSentence();
}
