package com.anysoftkeyboard.crashlytics;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;

import com.anysoftkeyboard.base.utils.Logger;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;

import io.fabric.sdk.android.Fabric;

public class NdkCrashlytics {

    public static final int SUPPORTED_MIN_SDK = Build.VERSION_CODES.ICE_CREAM_SANDWICH;

    @TargetApi(SUPPORTED_MIN_SDK)
    public NdkCrashlytics(Application application) {
        Logger.setLogProvider(new CrashlyticsLogProvider());
        Fabric.with(application, new Crashlytics(), new CrashlyticsNdk());
        Crashlytics.setString("locale", application.getResources().getConfiguration().locale.toString());
        Crashlytics.setString("installer-package-name", application.getPackageManager().getInstallerPackageName(application.getPackageName()));
    }

    public void destroy() {
    }
}
