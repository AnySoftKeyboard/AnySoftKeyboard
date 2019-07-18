package com.anysoftkeyboard.dictionaries.prefsprovider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.support.annotation.NonNull;
import com.anysoftkeyboard.dictionaries.content.AndroidUserDictionary;

class TappedAndroidUserDictionary extends AndroidUserDictionary {

    private final WordReadListener mWordsTapper;

    public TappedAndroidUserDictionary(
            Context context, String locale, WordReadListener wordsTapper) {
        super(context, locale);
        mWordsTapper = wordsTapper;
    }

    @Override
    protected void registerObserver(
            ContentObserver dictionaryContentObserver, ContentResolver contentResolver) {
        // DO NOT LISTEN TO CHANGES FROM THE OUTSIDE
    }

    @NonNull
    @Override
    protected WordReadListener createWordReadListener() {
        return mWordsTapper;
    }
}
