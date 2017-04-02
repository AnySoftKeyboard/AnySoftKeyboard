package com.anysoftkeyboard.ui.settings.wordseditor;

/**
* This will hold the data about locales in the Languages Spinner view
*/
final class DictionaryLocale {
    private final String mLocale;
    private final CharSequence mLocaleName;

    public DictionaryLocale(String locale, CharSequence name) {
        mLocale = locale;
        mLocaleName = name;
    }

    public String getLocale() {
        return mLocale;
    }

    @Override
    public String toString() {
        return String.format("%s - (%s)", mLocaleName, mLocale);
    }

    @Override
    public int hashCode() {
        return mLocale == null ? 0 : mLocale.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DictionaryLocale) {
            String otherLocale = ((DictionaryLocale) o).getLocale();
            if (otherLocale == null && mLocale == null)
                return true;
            else if (otherLocale == null)
                return false;
            else if (mLocale == null)
                return false;
            else
                return mLocale.equals(otherLocale);
        } else {
            return false;
        }
    }
}
