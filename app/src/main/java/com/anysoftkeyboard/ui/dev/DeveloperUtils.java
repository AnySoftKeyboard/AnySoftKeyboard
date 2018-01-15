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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.SharedPreferencesCompat;

import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.io.File;
import java.io.IOException;

public class DeveloperUtils {

    public static final String NEW_LINE = Logger.NEW_LINE;

    private static final String KEY_SDCARD_TRACING_ENABLED = "KEY_SDCARD_TRACING_ENABLED";
    private static final String ASK_TRACE_FILENAME = "AnySoftKeyboard_tracing.trace";
    private static final String ASK_MEM_DUMP_FILENAME = "ask_mem_dump.hprof";

    public static File createMemoryDump() throws IOException,
            UnsupportedOperationException {
        File extFolder = Environment.getExternalStorageDirectory();
        File target = new File(extFolder, ASK_MEM_DUMP_FILENAME);
        if (target.exists() && !target.delete()) throw new IOException("Failed to delete " + target);
        Debug.dumpHprofData(target.getAbsolutePath());
        return target;
    }

    public static boolean hasTracingRequested(Context applicationContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return prefs.getBoolean(KEY_SDCARD_TRACING_ENABLED, false);
    }

    public static void setTracingRequested(Context applicationContext, boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        Editor e = prefs.edit();
        e.putBoolean(KEY_SDCARD_TRACING_ENABLED, enabled);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(e);
    }

    private static boolean msTracingStarted = false;

    public static void startTracing() {
        Debug.startMethodTracing(getTraceFile().getAbsolutePath());
        msTracingStarted = true;
    }

    public static boolean hasTracingStarted() {
        return msTracingStarted;
    }

    public static void stopTracing() {
        try {
            Debug.stopMethodTracing();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.w("DEBUG_TOOLS", "Failed to stop method tracing. ", e);
        }
        msTracingStarted = false;
    }

    public static File getTraceFile() {
        File extFolder = Environment.getExternalStorageDirectory();
        return new File(extFolder, ASK_TRACE_FILENAME);
    }

    public static String getSysInfo(@Nullable Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("BRAND:").append(Build.BRAND).append(NEW_LINE);
        sb.append("DEVICE:").append(Build.DEVICE).append(NEW_LINE);
        sb.append("Build ID:").append(Build.DISPLAY).append(NEW_LINE);
        sb.append("changelist number:").append(Build.ID)
                .append("\n");
        sb.append("MODEL:").append(Build.MODEL).append(NEW_LINE);
        sb.append("PRODUCT:").append(Build.PRODUCT).append(NEW_LINE);
        sb.append("TAGS:").append(Build.TAGS).append(NEW_LINE);
        sb.append("VERSION.INCREMENTAL:")
                .append(Build.VERSION.INCREMENTAL).append(NEW_LINE);
        sb.append("VERSION.RELEASE:").append(Build.VERSION.RELEASE)
                .append(NEW_LINE);
        sb.append("VERSION.SDK_INT:").append(Build.VERSION.SDK_INT)
                .append(NEW_LINE);
        if (context != null && context.getResources() != null && context.getResources().getConfiguration() != null) {
            Configuration configuration = context.getResources().getConfiguration();
            sb.append("Locale:").append(configuration.locale).append(NEW_LINE);
            sb.append("configuration:").append(configuration.toString()).append(NEW_LINE);
        }

        sb.append("That's all I know.");
        return sb.toString();
    }

    public static String getAppDetails(Context appContext) {
        String appName = appContext.getString(R.string.ime_name) + " (" + appContext.getPackageName() + ")";
        try {
            PackageInfo info = appContext.getPackageManager().getPackageInfo(
                    appContext.getPackageName(), 0);
            appName = appName + " v" + info.versionName + " release " + info.versionCode;
            appName = appName + ". Installed on " + AnyApplication.getCurrentVersionInstallTime(appContext)
                    + ", first release installed was " + AnyApplication.getFirstAppVersionInstalled(appContext) + ".";
        } catch (NameNotFoundException e) {
            appName = "NA";
            e.printStackTrace();
        }
        return appName;
    }

    public static PackageInfo getPackageInfo(Context context) throws NameNotFoundException {
        return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
    }
}