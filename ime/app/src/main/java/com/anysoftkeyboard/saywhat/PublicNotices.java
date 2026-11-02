package com.anysoftkeyboard.saywhat;

import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.AnyApplication;
import java.util.ArrayList;
import java.util.List;

public abstract class PublicNotices extends AnySoftKeyboard {

  private static final String TAG = "ASKPublicNotices";

  private OnKey[] mOnKeyListeners = new OnKey[0];
  private OnVisible[] mOnVisibleListeners = new OnVisible[0];

  @Override
  public void onCreate() {
    super.onCreate();
    final List<OnKey> onKeys = new ArrayList<>();
    final List<OnVisible> onVisibles = new ArrayList<>();
    for (PublicNotice publicNotice : ((AnyApplication) getApplication()).getPublicNotices()) {
      if (publicNotice instanceof OnKey) onKeys.add((OnKey) publicNotice);
      if (publicNotice instanceof OnVisible) onVisibles.add((OnVisible) publicNotice);
    }
    mOnKeyListeners = onKeys.toArray(new OnKey[0]);
    mOnVisibleListeners = onVisibles.toArray(new OnVisible[0]);
  }

  @Override
  public void onKey(
      int primaryCode, Keyboard.Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
    super.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
    for (int i = 0; i < mOnKeyListeners.length; i++) {
      final OnKey onKey = mOnKeyListeners[i];
      if (onKey != null) {
        try {
          onKey.onKey(this, primaryCode, key);
        } catch (Throwable t) {
          Logger.w(TAG, t, "PublicNotice %s threw onKey! Disabling it.", onKey.getName());
          mOnKeyListeners[i] = null;
        }
      }
    }
  }

  @Override
  public void onStartInputView(EditorInfo attribute, boolean restarting) {
    super.onStartInputView(attribute, restarting);
    for (int i = 0; i < mOnVisibleListeners.length; i++) {
      final OnVisible onVisibleListener = mOnVisibleListeners[i];
      if (onVisibleListener != null) {
        try {
          onVisibleListener.onVisible(this, getCurrentKeyboard(), attribute);
        } catch (Throwable t) {
          Logger.w(
              TAG,
              t,
              "PublicNotice %s threw onVisible! Disabling it.",
              onVisibleListener.getName());
          mOnVisibleListeners[i] = null;
        }
      }
    }
  }

  @Override
  public void onFinishInputView(boolean finishingInput) {
    super.onFinishInputView(finishingInput);
    for (int i = 0; i < mOnVisibleListeners.length; i++) {
      final OnVisible onVisibleListener = mOnVisibleListeners[i];
      if (onVisibleListener != null) {
        try {
          onVisibleListener.onHidden(this, getCurrentKeyboard());
        } catch (Throwable t) {
          Logger.w(
              TAG,
              t,
              "PublicNotice %s threw onHidden! Disabling it.",
              onVisibleListener.getName());
          mOnVisibleListeners[i] = null;
        }
      }
    }
  }
}
