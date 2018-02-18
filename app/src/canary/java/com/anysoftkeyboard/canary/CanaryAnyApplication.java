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

package com.anysoftkeyboard.canary;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.anysoftkeyboard.crashlytics.NdkCrashlytics;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.AnyApplication;

import net.evendanan.chauffeur.lib.permissions.PermissionsFragmentChauffeurActivity;

public class CanaryAnyApplication extends AnyApplication {

    private NdkCrashlytics mNdkCrashlytics;

    @Override
    protected void setupCrashHandler(SharedPreferences sp) {
        super.setupCrashHandler(sp);
        if (Build.VERSION.SDK_INT >= NdkCrashlytics.SUPPORTED_MIN_SDK) {
            mNdkCrashlytics = new NdkCrashlytics(this);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mNdkCrashlytics != null) {
            mNdkCrashlytics.destroy();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent internetRequired = PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(this, MainSettingsActivity.class, CanaryPermissionsRequestCodes.INTERNET.getRequestCode(),
                Manifest.permission.INTERNET);
        if (internetRequired != null) startActivity(internetRequired);
    }
}
