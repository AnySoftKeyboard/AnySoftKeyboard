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
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.anysoftkeyboard.addons.AddOnImpl;
import com.anysoftkeyboard.addons.ScreenshotHolder;
import com.anysoftkeyboard.base.utils.Log;

public class KeyboardTheme extends AddOnImpl implements ScreenshotHolder {

    private static final String TAG = "ASK KBD-THEME";
    private final int mThemeResId;
    private final int mPopupThemeResId;
    private final int mIconsThemeResId;
    private final int mThemeScreenshotResId;

    public KeyboardTheme(Context askContext, Context packageContext, String id, int nameResId,
                         int themeResId, int popupThemeResId, int iconsThemeResId,
                         int themeScreenshotResId,
                         String description, int sortIndex) {
        super(askContext, packageContext, id, nameResId, description, sortIndex);

        mThemeResId = themeResId;
        mPopupThemeResId = popupThemeResId == -1 ? mThemeResId : popupThemeResId;
        mIconsThemeResId = iconsThemeResId;
        mThemeScreenshotResId = themeScreenshotResId;
    }

    public int getThemeResId() {
        return mThemeResId;
    }

    public int getPopupThemeResId() {
        return mPopupThemeResId;
    }

    public boolean hasScreenshot() {
        return (mThemeScreenshotResId != INVALID_RES_ID);
    }

    @Nullable
    public Drawable getScreenshot() {
        try {
            if (mThemeScreenshotResId != INVALID_RES_ID) {
                Context packageContext = getPackageContext();
                if (packageContext == null) return null;
                return packageContext.getResources().getDrawable(mThemeScreenshotResId);
            } else {
                return null;
            }
        } catch (Resources.NotFoundException n) {
            Log.w(TAG, "Failed to load pack Screenshot! ResId:" + mThemeScreenshotResId);
            return null;
        }
    }

    public int getIconsThemeResId() {
        return mIconsThemeResId;
    }
}