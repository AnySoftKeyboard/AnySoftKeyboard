package com.anysoftkeyboard.dictionaries.prefsprovider;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.dictionaries.content.AndroidUserDictionary;

class TappedAndroidUserDictionary extends AndroidUserDictionary {

    private final WordReadListener mWordsTapper;

    public TappedAndroidUserDictionary(Context context, String locale, WordReadListener wordsTapper) {
        super(context, locale);
        mWordsTapper = wordsTapper;
    }

    @NonNull
    @Override
    protected WordReadListener createWordReadListener() {
        return mWordsTapper;
    }
}
