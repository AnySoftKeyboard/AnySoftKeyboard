/*
 * Copyright (c) 2013 Menny Even-Danan
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

package com.anysoftkeyboard.keyboards;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnImpl;
import com.anysoftkeyboard.addons.IconHolder;
import com.anysoftkeyboard.addons.ScreenshotHolder;
import com.anysoftkeyboard.base.utils.Log;

public class KeyboardAddOnAndBuilder extends AddOnImpl implements IconHolder, ScreenshotHolder {

    public static final String KEYBOARD_PREF_PREFIX = "keyboard_";

    private static final String TAG = "ASK KBD-BUILDER";

    private final int mResId;
    private final int mLandscapeResId;
    private final int mIconResId;
    private final String mDefaultDictionary;
    private final int mQwertyTranslationId;
    private final String mAdditionalIsLetterExceptions;
    private final String mSentenceSeparators;
    private final boolean mKeyboardDefaultEnabled;
    private final int mScreenshotResId;

    public KeyboardAddOnAndBuilder(Context askContext, Context packageContext, String id, int nameResId,
                                   int layoutResId, int landscapeLayoutResId,
                                   String defaultDictionary, int iconResId,
                                   int physicalTranslationResId,
                                   String additionalIsLetterExceptions,
                                   String sentenceSeparators,
                                   String description,
                                   int keyboardIndex,
                                   boolean keyboardDefaultEnabled,
                                   int screenshotResId) {
        super(askContext, packageContext, KEYBOARD_PREF_PREFIX + id, nameResId, description, keyboardIndex);

        mResId = layoutResId;
        if (landscapeLayoutResId == AddOn.INVALID_RES_ID) {
            mLandscapeResId = mResId;
        } else {
            mLandscapeResId = landscapeLayoutResId;
        }

        mDefaultDictionary = defaultDictionary;
        mIconResId = iconResId;
        mAdditionalIsLetterExceptions = additionalIsLetterExceptions;
        mSentenceSeparators = sentenceSeparators;
        mQwertyTranslationId = physicalTranslationResId;
        mKeyboardDefaultEnabled = keyboardDefaultEnabled;
        mScreenshotResId = screenshotResId;
    }

    public boolean getKeyboardDefaultEnabled() {
        return mKeyboardDefaultEnabled;
    }

    public String getKeyboardLocale() {
        return mDefaultDictionary;
    }

    @Nullable
    public Drawable getIcon() {
        try {
            if (mIconResId != INVALID_RES_ID) {
                Context packageContext = getPackageContext();
                if (packageContext == null) return null;
                return packageContext.getResources().getDrawable(mIconResId);
            } else {
                return null;
            }
        } catch (Resources.NotFoundException n) {
            Log.w(TAG, "Failed to load pack ICON! ResId: " + mIconResId);
            return null;
        }
    }

    public boolean hasScreenshot() {
        return (mScreenshotResId != INVALID_RES_ID);
    }

    @Nullable
    public Drawable getScreenshot() {
        try {
            if (mScreenshotResId != INVALID_RES_ID) {
                Context packageContext = getPackageContext();
                if (packageContext == null) return null;
                return packageContext.getResources().getDrawable(mScreenshotResId);
            } else {
                return null;
            }
        } catch (Resources.NotFoundException n) {
            Log.w(TAG, "Failed to load pack screenshot! ResId: " + mScreenshotResId);
            return null;
        }
    }

    public AnyKeyboard createKeyboard(Context askContext, int mode) {
        return new ExternalAnyKeyboard(askContext, getPackageContext(), mResId, mLandscapeResId, getId(), getName(), mIconResId, mQwertyTranslationId, mDefaultDictionary, mAdditionalIsLetterExceptions, mSentenceSeparators, mode);
    }
}
