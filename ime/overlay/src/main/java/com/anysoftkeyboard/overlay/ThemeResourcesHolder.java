package com.anysoftkeyboard.overlay;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;

public interface ThemeResourcesHolder {
    ColorStateList getKeyTextColor();

    @ColorInt
    int getNameTextColor();

    @ColorInt
    int getHintTextColor();

    Drawable getKeyBackground();

    Drawable getKeyboardBackground();
}
