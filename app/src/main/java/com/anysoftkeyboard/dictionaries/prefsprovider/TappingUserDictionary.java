package com.anysoftkeyboard.dictionaries.prefsprovider;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.dictionaries.BTreeDictionary;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.dictionaries.content.AndroidUserDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.FallbackUserDictionary;

class TappingUserDictionary extends UserDictionary {

    private final BTreeDictionary.WordReadListener mWordsTapper;

    public TappingUserDictionary(Context context, String locale, BTreeDictionary.WordReadListener wordsTapper) {
        super(context, locale);
        mWordsTapper = wordsTapper;
    }

    @NonNull
    @Override
    protected AndroidUserDictionary createAndroidUserDictionary(Context context, String locale) {
        return new TappedAndroidUserDictionary(context, locale, mWordsTapper);
    }

    @NonNull
    @Override
    protected FallbackUserDictionary createFallbackUserDictionary(Context context, String locale) {
        return new TappedUserFallbackUserDictionary(context, locale, mWordsTapper);
    }
}
