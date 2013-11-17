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

package net.evendanan.pushingpixels;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;

import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PassengerFragmentSupport {

    private static final String TAG = "PassengerFragment";

    private static final String EXTRA_ORIGINATE_VIEW_CENTER = "EXTRA_ORIGINATE_VIEW_CENTER";
    private static final String EXTRA_ORIGINATE_VIEW_SCALE = "EXTRA_ORIGINATE_VIEW_SCALE";

    public static void setItemExpandExtraData(@Nonnull Fragment passengerFragment, float originateViewCenterX, float originateViewCenterY,
                                       float originateViewWidthScale, float originateViewHeightScale) {
        Bundle bundle = passengerFragment.getArguments();
        if (bundle == null) bundle = new Bundle();
        bundle.putParcelable(EXTRA_ORIGINATE_VIEW_CENTER, new PointF(originateViewCenterX, originateViewCenterY));
        bundle.putParcelable(EXTRA_ORIGINATE_VIEW_SCALE, new PointF(originateViewWidthScale, originateViewHeightScale));

        passengerFragment.setArguments(bundle);
    }

    public static @Nullable Animation onCreateAnimation(@Nonnull Fragment passengerFragment, int transit, boolean enter, int nextAnim) {
        Log.d(TAG, "onCreateAnimation: transit: " + transit + ", enter: " + enter + ", nextAnim: " + nextAnim);
        final boolean validTransitionToModify =
                nextAnim == R.anim.ui_context_expand_add_in || nextAnim == R.anim.ui_context_expand_pop_out;
        if (!validTransitionToModify) return null;

        ScaleAnimation scale = null;
        PointF originateViewCenterPoint = (PointF)passengerFragment.getArguments().get(EXTRA_ORIGINATE_VIEW_CENTER);
        PointF originateViewScale = (PointF)passengerFragment.getArguments().get(EXTRA_ORIGINATE_VIEW_SCALE);
        if (originateViewCenterPoint != null && originateViewScale != null) {
            Log.d(TAG, "originateViewCenterPoint: " + originateViewCenterPoint.toString());
            if (enter && nextAnim == R.anim.ui_context_expand_add_in) {
                scale = new ScaleAnimation(originateViewScale.x, 1.0f, originateViewScale.y, 1.0f,
                        ScaleAnimation.ABSOLUTE, originateViewCenterPoint.x,
                        ScaleAnimation.ABSOLUTE, originateViewCenterPoint.y);
            } else if (!enter && nextAnim == R.anim.ui_context_expand_pop_out) {
                scale = new ScaleAnimation(1.0f, originateViewScale.x, 1.0f, originateViewScale.y,
                        ScaleAnimation.ABSOLUTE, originateViewCenterPoint.x,
                        ScaleAnimation.ABSOLUTE, originateViewCenterPoint.y);
            }
        }

        if (scale == null) {
            //no originate view, so I'll add generic scale-animation
            if (enter) {
                scale = new ScaleAnimation(0.4f, 1.0f, 0.4f, 1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            } else {
                scale = new ScaleAnimation(1.0f, 0.4f, 1.0f, 0.4f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            }
        }
        scale.setDuration(passengerFragment.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(passengerFragment.getActivity().getApplicationContext(), nextAnim);
        set.addAnimation(scale);
        return set;
    }
}
