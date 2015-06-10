package com.anysoftkeyboard.nextword;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Utils {
    public static final String NEXT_WORD_SUGGESTION_OFF = "off";
    public static final String NEXT_WORD_SUGGESTION_WORDS = "words";
    public static final String NEXT_WORD_SUGGESTION_WORDS_AND_PUNCTUATIONS = "words_punctuations";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({NEXT_WORD_SUGGESTION_OFF, NEXT_WORD_SUGGESTION_WORDS, NEXT_WORD_SUGGESTION_WORDS_AND_PUNCTUATIONS})
    public @interface NextWordsSuggestionType {
    }

    @NonNull
    @NextWordsSuggestionType
    public static String getNextWordSuggestionTypeFromPrefs(@NonNull Resources resources, SharedPreferences defaultSharedPreferences) {
        final String defaultValue = resources.getString(R.string.settings_default_next_words_dictionary);
        final String prefsKey = resources.getString(R.string.settings_key_next_word_dictionary_type);
        //noinspection ResourceType
        return defaultSharedPreferences.getString(prefsKey, defaultValue);
    }

}
