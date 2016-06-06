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

import com.anysoftkeyboard.addons.AddOnImpl;

public class KeyboardTheme extends AddOnImpl {

    private static final String TAG = "ASK KBD-THEME";
    private final int mThemeResId;
    private final int mPopupThemeResId;
    private final int mIconsThemeResId;

    public KeyboardTheme(Context askContext, Context packageContext, String id, int nameResId,
                         int themeResId, int popupThemeResId, int iconsThemeResId,
                         String description, int sortIndex) {
        super(askContext, packageContext, id, nameResId, description, sortIndex);

        mThemeResId = themeResId;
        mPopupThemeResId = popupThemeResId == -1 ? mThemeResId : popupThemeResId;
        mIconsThemeResId = iconsThemeResId;
    }

    public int getThemeResId() {
        return mThemeResId;
    }

    public int getPopupThemeResId() {
        return mPopupThemeResId;
    }

    public int getIconsThemeResId() {
        return mIconsThemeResId;
    }
}