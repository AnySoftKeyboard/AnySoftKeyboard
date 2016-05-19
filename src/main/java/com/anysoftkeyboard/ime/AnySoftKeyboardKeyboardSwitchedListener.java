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
import android.view.inputmethod.InputMethodSubtype;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;

import java.util.List;

public abstract class AnySoftKeyboardKeyboardSwitchedListener extends AnySoftKeyboardBase
        implements KeyboardSwitcher.KeyboardSwitchedListener {

    private KeyboardSwitcher mKeyboardSwitcher;
    @Nullable
    private AnyKeyboard mCurrentAlphabetKeyboard;
    @Nullable
    private AnyKeyboard mCurrentSymbolsKeyboard;
    private boolean mInAlphabetKeyboardMode = true;
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;

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
        Log.w(TAG, "The OS has reported that it is low on memory!. I'll try to clear some cache.");
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
    public void onAlphabetKeyboardSet(@NonNull AnyKeyboard keyboard) {
        mCurrentAlphabetKeyboard = keyboard;
        mInAlphabetKeyboardMode = true;
        AnyApplication.getDeviceSpecific().reportCurrentInputMethodSubtypes(
                getInputMethodManager(),
                getSettingsInputMethodId(),
                getWindow().getWindow().getAttributes().token,
                keyboard);
    }

    @Override
    public void onSymbolsKeyboardSet(@NonNull AnyKeyboard keyboard) {
        mCurrentSymbolsKeyboard = keyboard;
        mInAlphabetKeyboardMode = false;
    }

    @Override
    public void onAvailableKeyboardsChanged(@NonNull List<KeyboardAddOnAndBuilder> builders) {
        AnyApplication.getDeviceSpecific().reportInputMethodSubtypes(getInputMethodManager(), getSettingsInputMethodId(), builders);
    }

    protected final boolean isInAlphabetKeyboardMode() {
        return mInAlphabetKeyboardMode;
    }

    protected final AnyKeyboard getCurrentAlphabetKeyboard() {
        return mCurrentAlphabetKeyboard;
    }

    protected final AnyKeyboard getCurrentSymbolsKeyboard() {
        return mCurrentSymbolsKeyboard;
    }

    protected final AnyKeyboard getCurrentKeyboard() {
        return mInAlphabetKeyboardMode ? mCurrentAlphabetKeyboard : mCurrentSymbolsKeyboard;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype) {
        super.onCurrentInputMethodSubtypeChanged(newSubtype);
        final String newSubtypeExtraValue = newSubtype.getExtraValue();
        if (TextUtils.isEmpty(newSubtypeExtraValue)) return;
        if (mCurrentAlphabetKeyboard == null || !newSubtypeExtraValue.equals(mCurrentAlphabetKeyboard.getKeyboardPrefId())) {
            mKeyboardSwitcher.nextAlphabetKeyboard(getCurrentInputEditorInfo(), newSubtypeExtraValue);
        }
    }
}
