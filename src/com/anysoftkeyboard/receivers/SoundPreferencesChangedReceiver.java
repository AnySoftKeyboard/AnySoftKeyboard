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

package com.anysoftkeyboard.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

public class SoundPreferencesChangedReceiver extends BroadcastReceiver {

    public static interface SoundPreferencesChangedListener {
        void updateRingerMode();
    }

    private final SoundPreferencesChangedListener mListener;

    public SoundPreferencesChangedReceiver(SoundPreferencesChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mListener.updateRingerMode();
    }

    public IntentFilter createFilterToRegisterOn() {
        return new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
    }
}
