/*
 * Copyright (c) 2016 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ime;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodSubtype;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.AnyApplication;

import java.util.List;

public abstract class AnySoftKeyboardKeyboardSwitchedListener extends AnySoftKeyboardPowerSaving
        implements KeyboardSwitcher.KeyboardSwitchedListener {

    private KeyboardSwitcher mKeyboardSwitcher;
    @Nullable
    private AnyKeyboard mCurrentAlphabetKeyboard;
    @Nullable
    private AnyKeyboard mCurrentSymbolsKeyboard;
    private boolean mInAlphabetKeyboardMode = true;
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;

    @Nullable
    private CharSequence mExpectedSubtypeChangeKeyboardId;

    @Override
    public void onCreate() {
        super.onCreate();

        mOrientation = getResources().getConfiguration().orientation;
        mKeyboardSwitcher = createKeyboardSwitcher();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != mOrientation) {
            mOrientation = newConfig.orientation;
            mKeyboardSwitcher.flushKeyboardsCache();
        }
    }

    @Override
    public void onLowMemory() {
        Logger.w(TAG, "The OS has reported that it is low on memory!. I'll try to clear some cache.");
        mKeyboardSwitcher.onLowMemory();
        super.onLowMemory();
    }

    @NonNull
    protected KeyboardSwitcher createKeyboardSwitcher() {
        return new KeyboardSwitcher(this, getApplicationContext());
    }

    protected final KeyboardSwitcher getKeyboardSwitcher() {
        return mKeyboardSwitcher;
    }

    @Override
    public void onAddOnsCriticalChange() {
        mKeyboardSwitcher.flushKeyboardsCache();
        super.onAddOnsCriticalChange();
    }

    @Override
    protected void onKeyboardThemeChanged(@NonNull KeyboardTheme theme) {
        super.onKeyboardThemeChanged(theme);
        if (getInputView() != null) {
            mKeyboardSwitcher.flushKeyboardsCache();
            hideWindow();
        }
    }

    @Override
    public void onAlphabetKeyboardSet(@NonNull AnyKeyboard keyboard) {
        mCurrentAlphabetKeyboard = keyboard;

        mInAlphabetKeyboardMode = true;
        //about to report, so setting what is the expected keyboard ID (to discard the event
        mExpectedSubtypeChangeKeyboardId = mCurrentAlphabetKeyboard.getKeyboardId();
        AnyApplication.getDeviceSpecific().reportCurrentInputMethodSubtypes(
                getInputMethodManager(),
                getSettingsInputMethodId(),
                getWindow().getWindow().getAttributes().token,
                keyboard.getLocale().toString(), keyboard.getKeyboardId());

        setKeyboardForView(keyboard);
    }

    @Override
    public void onSymbolsKeyboardSet(@NonNull AnyKeyboard keyboard) {
        mCurrentSymbolsKeyboard = keyboard;
        mInAlphabetKeyboardMode = false;
        setKeyboardForView(keyboard);
    }

    @Override
    public void onAvailableKeyboardsChanged(@NonNull List<KeyboardAddOnAndBuilder> builders) {
        AnyApplication.getDeviceSpecific().reportInputMethodSubtypes(getInputMethodManager(), getSettingsInputMethodId(), builders);
    }

    protected final boolean isInAlphabetKeyboardMode() {
        return mInAlphabetKeyboardMode;
    }

    /**
     * Returns the last set alphabet keyboard. Notice: this may be null if the
     * keyboard was not loaded it (say, in the start up of the IME service).
     */
    @Nullable
    protected final AnyKeyboard getCurrentAlphabetKeyboard() {
        return mCurrentAlphabetKeyboard;
    }

    /**
     * Returns the last set symbols keyboard. Notice: this may be null if the
     * keyboard was not loaded it (say, in the start up of the IME service).
     */
    @Nullable
    protected final AnyKeyboard getCurrentSymbolsKeyboard() {
        return mCurrentSymbolsKeyboard;
    }

    /**
     * Returns the last set symbols keyboard for the current mode (alphabet or symbols). Notice: this may be null if the
     * keyboard was not loaded it (say, in the start up of the IME service).
     */
    @Nullable
    protected final AnyKeyboard getCurrentKeyboard() {
        return mInAlphabetKeyboardMode ? mCurrentAlphabetKeyboard : mCurrentSymbolsKeyboard;
    }

    protected void setKeyboardForView(@NonNull AnyKeyboard keyboard) {
        final InputViewBinder inputView = getInputView();
        if (inputView != null) {
            inputView.setKeyboard(keyboard, mKeyboardSwitcher.peekNextAlphabetKeyboard(), mKeyboardSwitcher.peekNextSymbolsKeyboard());
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype) {
        super.onCurrentInputMethodSubtypeChanged(newSubtype);
        final String newSubtypeExtraValue = newSubtype.getExtraValue();
        if (TextUtils.isEmpty(newSubtypeExtraValue)) {
            return;//this might mean this is NOT AnySoftKeyboard subtype.
        }

        if (shouldConsumeSubtypeChangedEvent(newSubtypeExtraValue)) {
            mKeyboardSwitcher.nextAlphabetKeyboard(getCurrentInputEditorInfo(), newSubtypeExtraValue);
        }
    }

    protected boolean shouldConsumeSubtypeChangedEvent(String newSubtypeExtraValue) {
        //1) device is lower than ICE_CREAM_SANDWICH
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) return true;
        //2) we are NOT waiting for an expected report
        //https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/668
        //every time we change the alphabet keyboard, we want to OS to acknowledge
        //before we allow another subtype switch via event
        if (mExpectedSubtypeChangeKeyboardId != null) {
            if (mExpectedSubtypeChangeKeyboardId.equals(newSubtypeExtraValue)) {
                mExpectedSubtypeChangeKeyboardId = null;//got it!
            } else {
                //still waiting for the reported keyboard-id
                return false;
            }
        }
        //2) current alphabet keyboard is null
        if (mCurrentAlphabetKeyboard == null) return true;
        //3) (special - discarding) the requested subtype keyboard id is what we already have
        if (newSubtypeExtraValue.equals(mCurrentAlphabetKeyboard.getKeyboardId())) return false;

        //well, I guess we should do something with it
        return true;
    }

    @Override
    protected void onSharedPreferenceChange(String key) {
        super.onSharedPreferenceChange(key);

        if (key.startsWith(Keyboard.PREF_KEY_ROW_MODE_ENABLED_PREFIX)) {
            mKeyboardSwitcher.flushKeyboardsCache();
        }
    }

    @Override
    public View onCreateInputView() {
        View view = super.onCreateInputView();

        mKeyboardSwitcher.setInputView(getInputView());

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mKeyboardSwitcher.destroy();
        mKeyboardSwitcher = null;
    }
}
