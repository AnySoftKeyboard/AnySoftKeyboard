package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.drawable.Drawable;
import android.view.View;
import com.anysoftkeyboard.keyboards.Keyboard;

public class NullKeyPreviewsManager implements KeyPreviewsController {
  @Override
  public void hidePreviewForKey(Keyboard.Key key) {}

  @Override
  public void showPreviewForKey(
      Keyboard.Key key, Drawable icon, View parentView, PreviewPopupTheme previewPopupTheme) {}

  @Override
  public void showPreviewForKey(
      Keyboard.Key key, CharSequence label, View parentView, PreviewPopupTheme previewPopupTheme) {}

  @Override
  public void cancelAllPreviews() {}

  @Override
  public void destroy() {}
}
