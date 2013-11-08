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

package com.anysoftkeyboard.ui.dev;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;

import com.anysoftkeyboard.utils.Log;

import net.evendanan.pushingpixels.PassengerFragmentSupport;
import net.evendanan.pushingpixels.Passengerable;

public class LogCatViewFragment extends ListFragment implements Passengerable {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                inflater.getContext(), android.R.layout.simple_list_item_1,
                Log.getAllLogLinesList());
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void setItemExpandExtraData(float originateViewCenterX, float originateViewCenterY, float originateViewWidthScale, float originateViewHeightScale) {
        PassengerFragmentSupport.setItemExpandExtraData(this, originateViewCenterX, originateViewCenterY,
                originateViewWidthScale, originateViewHeightScale);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return PassengerFragmentSupport.onCreateAnimation(this, transit, enter, nextAnim);
    }
}
