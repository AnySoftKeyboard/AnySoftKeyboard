package com.anysoftkeyboard.saywhat;

import androidx.annotation.Nullable;
import com.anysoftkeyboard.keyboards.Keyboard;

public interface OnKey extends PublicNotice {
  void onKey(PublicNotices ime, int primaryCode, @Nullable Keyboard.Key key);
}
