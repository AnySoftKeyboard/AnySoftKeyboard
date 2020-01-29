package com.anysoftkeyboard.overlay;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;

public class StaticResourcesHolder implements ThemeResourcesHolder {
    private final ColorStateList mKeyTextColor;
    private final int mHintTextColor;
    private final int mNameTextColor;
    private final Drawable mKeyBackground;
    private final Drawable mKeyboardBackground;

    public StaticResourcesHolder(
            ColorStateList keyTextColor,
            int hintTextColor,
            int nameTextColor,
            Drawable keyBackground,
            Drawable keyboardBackground) {
        mKeyTextColor = keyTextColor;
        mHintTextColor = hintTextColor;
        mNameTextColor = nameTextColor;
        mKeyBackground = keyBackground;
        mKeyboardBackground = keyboardBackground;
    }

    @Override
    public ColorStateList getKeyTextColor() {
        return mKeyTextColor;
    }

    @Override
    public int getNameTextColor() {
        return mNameTextColor;
    }

    @Override
    public int getHintTextColor() {
        return mHintTextColor;
    }

    @Override
    public Drawable getKeyBackground() {
        return mKeyBackground;
    }

    @Override
    public Drawable getKeyboardBackground() {
        return mKeyboardBackground;
    }
}
