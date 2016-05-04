/*
 * Copyright (c) 2015 Menny Even-Danan
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

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.utils.Log;

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
    public void onAlphabetKeyboardSet(AnyKeyboard keyboard) {
        mCurrentAlphabetKeyboard = keyboard;
        mInAlphabetKeyboardMode = true;
    }

    @Override
    public void onSymbolsKeyboardSet(AnyKeyboard keyboard) {
        mCurrentSymbolsKeyboard = keyboard;
        mInAlphabetKeyboardMode = false;
    }

    protected final boolean isInAlphabetKeyboardMode() {return mInAlphabetKeyboardMode;}
    protected final AnyKeyboard getCurrentAlphabetKeyboard() {return mCurrentAlphabetKeyboard;}
    protected final AnyKeyboard getCurrentSymbolsKeyboard() {return mCurrentSymbolsKeyboard;}
    protected final AnyKeyboard getCurrentKeyboard() {
        return mInAlphabetKeyboardMode? mCurrentAlphabetKeyboard : mCurrentSymbolsKeyboard;
    }
}
