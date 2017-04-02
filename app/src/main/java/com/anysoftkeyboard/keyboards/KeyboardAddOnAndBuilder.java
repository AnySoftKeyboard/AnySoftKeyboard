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
import android.support.annotation.Nullable;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnImpl;

public class KeyboardAddOnAndBuilder extends AddOnImpl {

    private final int mResId;
    private final int mLandscapeResId;
    private final int mIconResId;
    private final String mDefaultDictionary;
    private final int mQwertyTranslationId;
    private final String mAdditionalIsLetterExceptions;
    private final String mSentenceSeparators;
    private final boolean mKeyboardDefaultEnabled;
    private final Context mAskContext;

    public KeyboardAddOnAndBuilder(Context askContext, Context packageContext, CharSequence id, CharSequence name,
                                   int layoutResId, int landscapeLayoutResId,
                                   String defaultDictionary, int iconResId,
                                   int physicalTranslationResId,
                                   String additionalIsLetterExceptions,
                                   String sentenceSeparators,
                                   CharSequence description,
                                   boolean isHidden,
                                   int keyboardIndex,
                                   boolean keyboardDefaultEnabled) {
        super(askContext, packageContext, id, name, description, isHidden, keyboardIndex);

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
        mAskContext = askContext;
    }

    public boolean getKeyboardDefaultEnabled() {
        return mKeyboardDefaultEnabled;
    }

    public String getKeyboardLocale() {
        return mDefaultDictionary;
    }

    public String getSentenceSeparators() {
        return mSentenceSeparators;
    }

    @Nullable
    public AnyKeyboard createKeyboard(@Keyboard.KeyboardRowModeId int mode) {
        Context remoteContext = getPackageContext();
        if (remoteContext == null) return null;
        return new ExternalAnyKeyboard(this, mAskContext, remoteContext, mResId, mLandscapeResId, getId(), getName(), mIconResId, mQwertyTranslationId, mDefaultDictionary, mAdditionalIsLetterExceptions, mSentenceSeparators, mode);
    }
}
