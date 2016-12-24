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
import android.os.Build;

import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.anysoftkeyboard.utils.Logger;
import com.crashlytics.android.Crashlytics;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;

import net.evendanan.chauffeur.lib.permissions.PermissionsFragmentChauffeurActivity;

import io.fabric.sdk.android.Fabric;

public class CanaryAnyApplication extends AnyApplication {

    @Override
    protected void setupCrashHandler() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            //replacing the default crash-handler with Crashlytics.
            Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
            Crashlytics.setString("locale", getResources().getConfiguration().locale.toString());
            Crashlytics.setString("installer-package-name", getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID));
            Logger.setLogProvider(new CrashlyticsLogProvider());
        } else {
            super.setupCrashHandler();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent internetRequired = PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(this, MainSettingsActivity.class, CanaryPermissionsRequestCodes.INTERNET.getRequestCode(), Manifest.permission.INTERNET);
        if (internetRequired != null) startActivity(internetRequired);
    }
}
