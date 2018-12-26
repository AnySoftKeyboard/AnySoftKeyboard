package com.anysoftkeyboard.overlay;

import java.util.Locale;

public class OverlayData {
    private int mPrimaryColor = 0;
    private int mPrimaryDarkColor = 0;
    private int mAccentColor = 0;
    private int mPrimaryTextColor = 0;
    private int mSecondaryTextColor = 0;

    /**
     * The remote app primary color for text.
     */
    public int getPrimaryTextColor() {
        return mPrimaryTextColor;
    }

    void setPrimaryTextColor(int primaryTextColor) {
        mPrimaryTextColor = primaryTextColor;
    }

    /**
     * The remote app secondary color for text.
     */
    public int getSecondaryTextColor() {
        return mSecondaryTextColor;
    }

    void setSecondaryTextColor(int textColor) {
        mSecondaryTextColor = textColor;
    }

    /**
     * The remote app accent (activated) color.
     */
    public int getAccentColor() {
        return mAccentColor;
    }

    void setAccentColor(int color) {
        mAccentColor = color;
    }

    /**
     * The remote app primary elements color.
     */
    public int getPrimaryColor() {
        return mPrimaryColor;
    }

    void setPrimaryColor(int primaryColor) {
        mPrimaryColor = primaryColor;
    }

    /**
     * The remote app darker-primary elements color.
     */
    public int getPrimaryDarkColor() {
        return mPrimaryDarkColor;
    }

    void setPrimaryDarkColor(int primaryDarkColor) {
        mPrimaryDarkColor = primaryDarkColor;
    }

    public boolean isValid() {
        return (mPrimaryColor != mPrimaryTextColor) && (mPrimaryDarkColor != mPrimaryTextColor);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Overlay primary-color %s, dark-primary-color %s, primary text color %s, secondary text color %s (is valid %b)",
                Integer.toHexString(getPrimaryColor()), Integer.toHexString(getPrimaryDarkColor()), Integer.toHexString(getPrimaryTextColor()), Integer.toHexString(getSecondaryTextColor()),
                isValid());
    }
}
