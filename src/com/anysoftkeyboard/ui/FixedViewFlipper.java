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

package com.anysoftkeyboard.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;
import com.menny.android.anysoftkeyboard.R;

/*
 * http://daniel-codes.blogspot.com/2010/05/viewflipper-receiver-not-registered.html
 */
public class FixedViewFlipper extends ViewFlipper {

    public FixedViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        } catch (IllegalArgumentException e) {
            stopFlipping();
        }
    }

    @Override
    public void setDisplayedChild(int whichChild) {
        final int current = getDisplayedChild();
        if (current == whichChild) return;
        final Context c = getContext().getApplicationContext();
        if (whichChild > current) {
            setInAnimation(c, R.anim.slide_in_right);
            setOutAnimation(c, R.anim.slide_out_left);
        } else if (whichChild < current) {
            setInAnimation(c, R.anim.slide_in_left);
            setOutAnimation(c, R.anim.slide_out_right);
        }
        super.setDisplayedChild(whichChild);
    }
}
