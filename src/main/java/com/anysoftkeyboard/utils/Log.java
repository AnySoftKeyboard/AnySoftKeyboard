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

package com.anysoftkeyboard.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.FeaturesSet;

import java.util.ArrayList;

public class Log {
    private static final boolean DEBUG = FeaturesSet.DEBUG_LOG;

    private static final StringBuilder msFormatBuilder = new StringBuilder(1024);
    private static final java.util.Formatter msFormatter =  new java.util.Formatter(msFormatBuilder);

    private static final String[] msLogs = new String[FeaturesSet.DEBUG_LOG ? 225 : 0];
    private static int msLogIndex = 0;

    private synchronized static void addLog(String level, String tag, String message) {
        if (DEBUG) {
            msLogs[msLogIndex] = System.currentTimeMillis() + "-" + level + "-[" + tag + "] " + message;
            msLogIndex = (msLogIndex + 1) % msLogs.length;
        }
    }

    private synchronized static void addLog(String level, String tag, String message, Throwable t) {
        if (DEBUG) {
            addLog(level, tag, message);
            addLog(level, tag, getStackTrace(t));
        }
    }

    @NonNull
    public synchronized static ArrayList<String> getAllLogLinesList() {
        ArrayList<String> lines = new ArrayList<String>(msLogs.length);
        if (msLogs.length > 0) {
            int index = msLogIndex;
            do {
                index--;
                if (index == -1) index = msLogs.length - 1;
                String logLine = msLogs[index];
                if (logLine == null)
                    break;
                lines.add(msLogs[index]);
            }
            while (index != msLogIndex);
        }
        return lines;
    }

    @NonNull
    public synchronized static String getAllLogLines() {
        if (DEBUG) {
            ArrayList<String> lines = getAllLogLinesList();
            //now to build the string
            StringBuilder sb = new StringBuilder("Log contains " + lines.size() + " lines:");
            final String newline = DeveloperUtils.NEW_LINE;
            while (lines.size() > 0) {
                String line = lines.remove(lines.size() - 1);
                sb.append(newline);
                sb.append(line);
            }
            return sb.toString();
        } else {
            return "Not supported in RELEASE mode!";
        }
    }

    private Log() {
        //no instances please.
    }

    private static String LVL_V = "V";

    public static void v(String TAG, String text, Object... args) {
        if (DEBUG) {
            String msg = args == null? text : msFormatter.format(text, args).toString();
            msFormatBuilder.setLength(0);
            android.util.Log.v(TAG, msg);
            addLog(LVL_V, TAG, msg);
        }
    }

    public static void v(String TAG, String text, Throwable t) {
        if (DEBUG) {
            android.util.Log.v(TAG, text, t);
            addLog(LVL_V, TAG, text, t);
        }
    }

    private static String LVL_D = "D";

    public static void d(String TAG, String text) {
        if (DEBUG) {
            android.util.Log.d(TAG, text);
            addLog(LVL_D, TAG, text);
        }
    }

    public static void d(String TAG, String text, Object... args) {
        if (DEBUG) {
            String msg = args == null? text : msFormatter.format(text, args).toString();
            msFormatBuilder.setLength(0);
            android.util.Log.d(TAG, msg);
            addLog(LVL_D, TAG, msg);
        }
    }

    public static void d(String TAG, String text, Throwable t) {
        if (DEBUG) {
            android.util.Log.d(TAG, text, t);
            addLog(LVL_D, TAG, text, t);
        }
    }

    private static String LVL_I = "I";

    public static void i(String TAG, String text, Object... args) {
        String msg = args == null? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        android.util.Log.i(TAG, msg);
        addLog(LVL_I, TAG, msg);
    }

    public static void i(String TAG, String text, Throwable t) {
        android.util.Log.i(TAG, text, t);
        addLog(LVL_I, TAG, text, t);
    }

    private static String LVL_W = "W";

    public static void w(String TAG, String text, Object... args) {
        String msg = args == null? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        android.util.Log.w(TAG, msg);
        addLog(LVL_W, TAG, msg);
    }

    public static void w(String TAG, String text, Throwable t) {
        android.util.Log.w(TAG, text, t);
        addLog(LVL_W, TAG, text, t);
    }

    private static String LVL_E = "E";

    public static void e(String TAG, String text, Object... args) {
        String msg = args == null? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        android.util.Log.e(TAG, msg);
        addLog(LVL_E, TAG, msg);
    }

    //TODO: remove this method
    public static void e(String TAG, String text, Throwable t) {
        android.util.Log.e(TAG, text, t);
        addLog(LVL_E, TAG, text, t);
    }

    public static void w(String TAG, Throwable e, String text, Object... args) {
        String msg = args == null? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        android.util.Log.e(TAG, msg, e);
        addLog(LVL_E, TAG, msg);
    }

    private static String LVL_WTF = "WTF";

    @TargetApi(8)
    public static void wtf(String TAG, String text, Object... args) {
        String msg = args == null? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        addLog(LVL_WTF, TAG, msg);
        if (Build.VERSION.SDK_INT >= 8)
            android.util.Log.wtf(TAG, msg);
        else if (BuildConfig.DEBUG)
            throw new RuntimeException(msg);
        else
            android.util.Log.e(TAG, msg);
    }

    @TargetApi(8)
    public static void wtf(String TAG, String text, Throwable t) {
        addLog(LVL_WTF, TAG, text, t);
        if (Build.VERSION.SDK_INT >= 8)
            android.util.Log.wtf(TAG, text, t);
        else if (BuildConfig.DEBUG)
            throw new RuntimeException(text, t);
        else
            android.util.Log.e(TAG, text, t);
    }


    public static String getStackTrace(Throwable ex) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        StringBuilder sb = new StringBuilder();

        for (StackTraceElement element : stackTrace) {
            sb.append(element.toString());
            sb.append('\n');
        }

        if (ex.getCause() == null)
            return sb.toString();
        else {
            ex = ex.getCause();
            String cause = getStackTrace(ex);
            sb.append("*** Cause: " + ex.getClass().getName());
            sb.append('\n');
            sb.append("** Message: " + ex.getMessage());
            sb.append('\n');
            sb.append("** Stack track: " + cause);
            sb.append('\n');
            return sb.toString();
        }
    }
}
