package com.anysoftkeyboard.nextword;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

interface NextWordsFileParser {
    @NonNull
    Iterable<NextWordsContainer> loadStoredNextWords(InputStream inputStream) throws IOException;

    void storeNextWords(@NonNull Iterable<NextWordsContainer> nextWords, OutputStream outputStream)
            throws IOException;
}
