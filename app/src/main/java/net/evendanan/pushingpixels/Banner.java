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

/* The following code was written by Matthew Wiggins
 * and is released under the APACHE 2.0 license
 *
 * additional code was written by Menny Even Danan, and is also released under APACHE 2.0 license
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package net.evendanan.pushingpixels;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * A custom view that ensures that its image is taking all the space it can, without losing image ratio.
 * Make sure you use layout_width="match_parent" and layout_height="wrap_content" if you want the width to be the ratio guideline,
 * or layout_height="match_parent" and layout_width="wrap_content" if you want the height to be the guideline.
 *
 * Idea taken from: http://stackoverflow.com/a/2997760/1324235
 *
 * @author Menny Even-Danan
 */
public class Banner extends ImageView {
    public Banner(Context context) {
        this(context, null);
    }

    public Banner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Banner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //I'm using FIT_XY since I'm going to set the view's dimensions based on the intrinsic dimensions of the drawable
        //so, this will ensure that the image is taking the entire view's space, and my code in onMeasure will make sure ratio is kept.
        setScaleType(ScaleType.FIT_XY);
    }

    @Override protected void onMeasure(int widthMeasureSpec,
                                       int heightMeasureSpec) {
        final ViewGroup.LayoutParams lp = getLayoutParams();
        if (    ((lp.width == 0 || lp.width == ViewGroup.LayoutParams.MATCH_PARENT) && lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) ||
                ((lp.height == 0 || lp.height == ViewGroup.LayoutParams.MATCH_PARENT) && lp.width == ViewGroup.LayoutParams.WRAP_CONTENT)){
            final Drawable banner = getDrawable();
            if (banner == null) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            } else {
                final int height;
                final int width;
                if (lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    width = MeasureSpec.getSize(widthMeasureSpec);
                    height = width * banner.getIntrinsicHeight() / banner.getIntrinsicWidth();
                } else {
                    height = MeasureSpec.getSize(widthMeasureSpec);
                    width = height * banner.getIntrinsicWidth() / banner.getIntrinsicHeight();
                }
                setMeasuredDimension(width, height);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}