package com.anysoftkeyboard.ime;

import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.base.utils.Logger;

public abstract class AnySoftKeyboardIncognito extends AnySoftKeyboardWithGestureTyping {

    private boolean mUserEnabledIncognito = false;
    private static final int INCOGNITO_FLAGS =
            EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING |
                    EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD |
                    EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD |
                    EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD |
                    EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD;

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);

        if ((info.imeOptions & INCOGNITO_FLAGS) != 0) {
            Logger.d(TAG, "IME_FLAG_NO_PERSONALIZED_LEARNING is set. Switching to incognito.");
            setIncognito(true, false);
        } else {
            setIncognito(mUserEnabledIncognito, false);
        }
    }

    /**
     * Sets the incognito-mode for the keyboard.
     * Sets all incognito-related attributes. Respects user choice saved in mUserEnabledIncognito
     *
     * @param enable The boolean value the incognito mode should be set to
     * @param byUser True when set by the user, false when automatically invoked.
     */
    protected void setIncognito(boolean enable, boolean byUser) {
        mSuggest.setIncognitoMode(enable);
        getQuickKeyHistoryRecords().setIncognitoMode(mSuggest.isIncognitoMode());
        setupInputViewWatermark();
        if (byUser) {
            mUserEnabledIncognito = enable;
        }
    }
}
