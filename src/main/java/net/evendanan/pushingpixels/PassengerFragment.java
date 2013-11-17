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

import android.support.v4.app.Fragment;
import android.view.animation.Animation;

public abstract class PassengerFragment extends Fragment implements Passengerable {

    private static final String TAG = "PassengerFragment";

    @Override
    public void setItemExpandExtraData(float originateViewCenterX, float originateViewCenterY,
                                       float originateViewWidthScale, float originateViewHeightScale) {
        PassengerFragmentSupport.setItemExpandExtraData(this,
                originateViewCenterX, originateViewCenterY,
                originateViewWidthScale, originateViewHeightScale);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation customAnimation = PassengerFragmentSupport.onCreateAnimation(this, transit, enter, nextAnim);
        if (customAnimation == null)
            return super.onCreateAnimation(transit, enter, nextAnim);
        else
            return customAnimation;
    }
}
