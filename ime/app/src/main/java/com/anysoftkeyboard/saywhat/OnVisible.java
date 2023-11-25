package com.anysoftkeyboard.saywhat;

import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.keyboards.AnyKeyboard;

public interface OnVisible extends PublicNotice {
  void onVisible(PublicNotices ime, AnyKeyboard keyboard, EditorInfo editorInfo);

  void onHidden(PublicNotices ime, AnyKeyboard keyboard);
}
