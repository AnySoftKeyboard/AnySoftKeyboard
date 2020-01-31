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

package com.anysoftkeyboard.theme;

import android.content.Context;
import android.support.annotation.StyleRes;
import com.anysoftkeyboard.addons.AddOnImpl;

public class KeyboardTheme extends AddOnImpl {

    @StyleRes private final int mThemeResId;
    @StyleRes private final int mPopupThemeResId;
    @StyleRes private final int mIconsThemeResId;
    @StyleRes private final int mPopupIconsThemeResId;

    public KeyboardTheme(
            Context askContext,
            Context packageContext,
            int apiVersion,
            CharSequence id,
            CharSequence name,
            @StyleRes int themeResId,
            @StyleRes int popupThemeResId,
            @StyleRes int iconsThemeResId,
            @StyleRes int popupIconsThemeResId,
            boolean isHidden,
            CharSequence description,
            int sortIndex) {
        super(askContext, packageContext, apiVersion, id, name, description, isHidden, sortIndex);

        mThemeResId = themeResId;
        mPopupThemeResId = popupThemeResId == INVALID_RES_ID ? mThemeResId : popupThemeResId;
        mIconsThemeResId = iconsThemeResId;
        mPopupIconsThemeResId =
                popupIconsThemeResId == INVALID_RES_ID ? mIconsThemeResId : popupIconsThemeResId;
    }

    @StyleRes
    public int getThemeResId() {
        return mThemeResId;
    }

    @StyleRes
    public int getPopupThemeResId() {
        return mPopupThemeResId;
    }

    @StyleRes
    public int getIconsThemeResId() {
        return mIconsThemeResId;
    }

    @StyleRes
    public int getPopupIconsThemeResId() {
        return mPopupIconsThemeResId;
    }
}
