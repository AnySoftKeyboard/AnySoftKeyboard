package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.drawable.Drawable;

import com.anysoftkeyboard.keyboards.Keyboard;

public interface KeyPreviewsController {
    void hidePreviewForKey(Keyboard.Key key);

    void showPreviewForKey(Keyboard.Key key, Drawable icon);

    void showPreviewForKey(Keyboard.Key key, CharSequence label);

    void cancelAllPreviews();

    void resetTheme();

    void destroy();
}
