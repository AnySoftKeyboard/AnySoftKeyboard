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

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import com.anysoftkeyboard.theme.KeyboardTheme;

public class DrawableBuilder {
    private final int mDrawableResourceId;
    private final KeyboardTheme mTheme;

    private DrawableBuilder(KeyboardTheme theme, int drawableResId) {
        mTheme = theme;
        mDrawableResourceId = drawableResId;
    }

    public Drawable buildDrawable() {
        return mTheme.getPackageContext().getResources().getDrawable(mDrawableResourceId);
    }

    public static DrawableBuilder build(KeyboardTheme theme, TypedArray a, final int index) {
        int resId = a.getResourceId(index, 0);
        return new DrawableBuilder(theme, resId);
    }
}
