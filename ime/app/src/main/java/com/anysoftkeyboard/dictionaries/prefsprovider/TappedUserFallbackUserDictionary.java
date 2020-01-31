package com.anysoftkeyboard.dictionaries.prefsprovider;

import android.content.Context;
import android.support.annotation.NonNull;
import com.anysoftkeyboard.dictionaries.sqlite.FallbackUserDictionary;

class TappedUserFallbackUserDictionary extends FallbackUserDictionary {

    private final WordReadListener mWordsTapper;

    public TappedUserFallbackUserDictionary(
            Context context, String locale, WordReadListener wordsTapper) {
        super(context, locale);
        mWordsTapper = wordsTapper;
    }

    @NonNull
    @Override
    protected WordReadListener createWordReadListener() {
        return mWordsTapper;
    }
}
