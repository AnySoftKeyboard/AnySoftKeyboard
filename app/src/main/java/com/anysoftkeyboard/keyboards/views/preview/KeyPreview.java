package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.anysoftkeyboard.keyboards.Keyboard;

public interface KeyPreview {
    void showPreviewForKey(Keyboard.Key key, CharSequence label, Point previewPosition);

    void showPreviewForKey(Keyboard.Key key, Drawable icon, Point previewPosition);

    void dismiss();
}
