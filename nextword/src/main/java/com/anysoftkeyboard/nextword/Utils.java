package com.anysoftkeyboard.nextword;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Utils {
    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String NEXT_WORD_SUGGESTION_OFF = "off";
    public static final String NEXT_WORD_SUGGESTION_WORDS = "words";
    public static final String NEXT_WORD_SUGGESTION_WORDS_AND_PUNCTUATIONS = "words_punctuations";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({NEXT_WORD_SUGGESTION_OFF, NEXT_WORD_SUGGESTION_WORDS, NEXT_WORD_SUGGESTION_WORDS_AND_PUNCTUATIONS})
    public @interface NextWordsSuggestionType {
    }


    public static final String NEXT_WORD_SUGGESTION_AGGRESSIVENESS_MINIMAL = "minimal_aggressiveness";
    public static final String NEXT_WORD_SUGGESTION_AGGRESSIVENESS_MEDIUM = "medium_aggressiveness";
    public static final String NEXT_WORD_SUGGESTION_AGGRESSIVENESS_MAXIMUM = "maximum_aggressiveness";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({NEXT_WORD_SUGGESTION_AGGRESSIVENESS_MINIMAL, NEXT_WORD_SUGGESTION_AGGRESSIVENESS_MEDIUM, NEXT_WORD_SUGGESTION_AGGRESSIVENESS_MAXIMUM})
    public @interface NextWordsSuggestionAggressivenessType {
    }

    @NonNull
    @NextWordsSuggestionType
    public static String getNextWordSuggestionTypeFromPrefs(@NonNull Resources resources, SharedPreferences defaultSharedPreferences) {
        final String defaultValue = resources.getString(R.string.settings_default_next_words_dictionary_type);
        final String prefsKey = resources.getString(R.string.settings_key_next_word_dictionary_type);
        //noinspection ResourceType
        return defaultSharedPreferences.getString(prefsKey, defaultValue);
    }

    @NonNull
    @NextWordsSuggestionAggressivenessType
    private static String getNextWordSuggestionAggressivenessFromPrefs(@NonNull Resources resources, SharedPreferences defaultSharedPreferences) {
        final String defaultValue = resources.getString(R.string.settings_default_next_word_suggestion_aggressiveness);
        final String prefsKey = resources.getString(R.string.settings_key_next_word_suggestion_aggressiveness);
        //noinspection ResourceType
        return defaultSharedPreferences.getString(prefsKey, defaultValue);
    }

    public static int getNextWordSuggestionCountFromPrefs(@NonNull Resources resources, @NonNull SharedPreferences defaultSharedPreferences) {
        switch (getNextWordSuggestionAggressivenessFromPrefs(resources, defaultSharedPreferences)) {
            case NEXT_WORD_SUGGESTION_AGGRESSIVENESS_MINIMAL:
                return 3;
            case NEXT_WORD_SUGGESTION_AGGRESSIVENESS_MEDIUM:
                return 5;
            case NEXT_WORD_SUGGESTION_AGGRESSIVENESS_MAXIMUM:
                return 8;
            default:
                return 3;
        }
    }

    public static int getNextWordSuggestionMinUsageFromPrefs(@NonNull Resources resources, @NonNull SharedPreferences defaultSharedPreferences) {
        switch (getNextWordSuggestionAggressivenessFromPrefs(resources, defaultSharedPreferences)) {
            case NEXT_WORD_SUGGESTION_AGGRESSIVENESS_MINIMAL:
                return 5;
            case NEXT_WORD_SUGGESTION_AGGRESSIVENESS_MEDIUM:
                return 3;
            case NEXT_WORD_SUGGESTION_AGGRESSIVENESS_MAXIMUM:
                return 1;
            default:
                return 5;
        }
    }

}
