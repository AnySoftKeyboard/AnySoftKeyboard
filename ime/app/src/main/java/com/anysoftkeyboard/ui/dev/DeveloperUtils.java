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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Debug;
import android.os.Environment;
import androidx.annotation.NonNull;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.prefs.DirectBootAwareSharedPreferences;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.io.File;
import java.io.IOException;

public class DeveloperUtils {

    public static final String NEW_LINE = Logger.NEW_LINE;

    private static final String KEY_SDCARD_TRACING_ENABLED = "KEY_SDCARD_TRACING_ENABLED";
    private static final String ASK_TRACE_FILENAME = "AnySoftKeyboard_tracing.trace";
    private static final String ASK_MEM_DUMP_FILENAME = "ask_mem_dump.hprof";
    private static boolean msTracingStarted = false;

    public static File createMemoryDump() throws IOException, UnsupportedOperationException {
        File extFolder = Environment.getExternalStorageDirectory();
        File target = new File(extFolder, ASK_MEM_DUMP_FILENAME);
        if (target.exists() && !target.delete())
            throw new IOException("Failed to delete " + target);
        Debug.dumpHprofData(target.getAbsolutePath());
        return target;
    }

    public static boolean hasTracingRequested(Context applicationContext) {
        return DirectBootAwareSharedPreferences.create(applicationContext)
                .getBoolean(KEY_SDCARD_TRACING_ENABLED, false);
    }

    public static void setTracingRequested(Context applicationContext, boolean enabled) {
        DirectBootAwareSharedPreferences.create(applicationContext)
                .edit()
                .putBoolean(KEY_SDCARD_TRACING_ENABLED, enabled)
                .apply();
    }

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

    @NonNull public static String getAppDetails(@NonNull Context context) {
        StringBuilder appName = new StringBuilder();
        appName.append(context.getString(R.string.ime_name))
                .append(" (")
                .append(context.getPackageName())
                .append(")");
        try {
            PackageInfo info =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appName.append(" v")
                    .append(info.versionName)
                    .append(" release ")
                    .append(info.versionCode);
            appName.append(". Installed on ")
                    .append(AnyApplication.getCurrentVersionInstallTime(context))
                    .append(", first release installed was ")
                    .append(AnyApplication.getFirstAppVersionInstalled(context))
                    .append(".");
        } catch (PackageManager.NameNotFoundException e) {
            appName.append(" !!! Error with package info !!! ");
            e.printStackTrace();
        }
        return appName.toString();
    }
}
