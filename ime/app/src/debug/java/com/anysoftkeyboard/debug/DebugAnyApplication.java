/*
 * Copyright (c) 2016 Menny Even-Danan
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

package com.anysoftkeyboard.debug;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;

public class DebugAnyApplication extends AnyApplication {

    @Override
    protected void setupCrashHandler(SharedPreferences sp) {
        super.setupCrashHandler(sp);
        Logger.setLogProvider(new LogCatLogProvider());
    }

    @Override
    public List<Drawable> getInitialWatermarksList() {
        List<Drawable> watermarks = super.getInitialWatermarksList();
        watermarks.add(ContextCompat.getDrawable(this, R.drawable.ic_watermark_dev_build));

        return watermarks;
    }
}
