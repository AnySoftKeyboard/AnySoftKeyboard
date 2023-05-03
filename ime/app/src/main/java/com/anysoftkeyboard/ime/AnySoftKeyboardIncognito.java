package com.anysoftkeyboard.ime;

import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.base.utils.Logger;

public abstract class AnySoftKeyboardIncognito extends AnySoftKeyboardWithGestureTyping {

  private boolean mUserEnabledIncognito = false;

  private static final int NUMBER_INCOGNITO_TYPE =
      EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD;

  @Override
  public void onStartInputView(EditorInfo info, boolean restarting) {
    super.onStartInputView(info, restarting);

    if (isNoPersonalizedLearning(info) || isTextPassword(info) || isNumberPassword(info)) {
      Logger.d(TAG, "IME_FLAG_NO_PERSONALIZED_LEARNING is set. Switching to incognito.");
      setIncognito(true, false);
    } else {
      setIncognito(mUserEnabledIncognito, false);
    }
  }

  protected static boolean isNumberPassword(EditorInfo info) {
    return (info.inputType & NUMBER_INCOGNITO_TYPE) == NUMBER_INCOGNITO_TYPE;
  }

  private static boolean isNoPersonalizedLearning(EditorInfo info) {
    return (info.imeOptions & EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0;
  }

  /**
   * Sets the incognito-mode for the keyboard. Sets all incognito-related attributes. Respects user
   * choice saved in mUserEnabledIncognito
   *
   * @param enable The boolean value the incognito mode should be set to
   * @param byUser True when set by the user, false when automatically invoked.
   */
  protected void setIncognito(boolean enable, boolean byUser) {
    getSuggest().setIncognitoMode(enable);
    getQuickKeyHistoryRecords().setIncognitoMode(getSuggest().isIncognitoMode());
    setupInputViewWatermark();
    if (byUser) {
      mUserEnabledIncognito = enable;
    }
  }
}
