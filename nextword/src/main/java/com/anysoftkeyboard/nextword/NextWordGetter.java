package com.anysoftkeyboard.nextword;

public interface NextWordGetter {
    Iterable<String> getNextWords(CharSequence currentWord, int maxResults, int minWordUsage);
}
