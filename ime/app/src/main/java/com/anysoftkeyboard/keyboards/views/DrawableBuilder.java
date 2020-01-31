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

package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import com.anysoftkeyboard.theme.KeyboardTheme;

public class DrawableBuilder {
    private final int mDrawableResourceId;
    private final KeyboardTheme mTheme;
    @Nullable private Drawable mDrawable;

    private DrawableBuilder(KeyboardTheme theme, int drawableResId) {
        mTheme = theme;
        mDrawableResourceId = drawableResId;
    }

    @Nullable
    public Drawable buildDrawable() {
        if (mDrawable != null) return mDrawable;
        final Context packageContext = mTheme.getPackageContext();
        if (packageContext == null) return null;
        mDrawable = ContextCompat.getDrawable(packageContext, mDrawableResourceId);
        return mDrawable;
    }

    public static DrawableBuilder build(KeyboardTheme theme, TypedArray a, final int index) {
        int resId = a.getResourceId(index, 0);
        if (resId == 0)
            throw new IllegalArgumentException("No resource ID was found at index " + index);
        return new DrawableBuilder(theme, resId);
    }
}
