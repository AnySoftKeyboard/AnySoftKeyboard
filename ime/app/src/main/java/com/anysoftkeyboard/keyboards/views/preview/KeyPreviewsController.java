package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.drawable.Drawable;
import android.view.View;
import com.anysoftkeyboard.keyboards.Keyboard;

public interface KeyPreviewsController {
  void hidePreviewForKey(Keyboard.Key key);

  void showPreviewForKey(
      Keyboard.Key key, Drawable icon, View parentView, PreviewPopupTheme previewPopupTheme);

  void showPreviewForKey(
      Keyboard.Key key, CharSequence label, View parentView, PreviewPopupTheme previewPopupTheme);

  void cancelAllPreviews();

  void destroy();
}
