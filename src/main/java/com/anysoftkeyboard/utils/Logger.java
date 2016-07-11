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

import android.support.annotation.NonNull;

import com.menny.android.anysoftkeyboard.BuildConfig;

import java.util.ArrayList;
import java.util.Locale;

public class Logger {
    public static final String NEW_LINE = System.getProperty("line.separator");

    private static final StringBuilder msFormatBuilder = new StringBuilder(1024);
    private static final java.util.Formatter msFormatter = new java.util.Formatter(msFormatBuilder, Locale.US);

    private static final String[] msLogs = new String[BuildConfig.TESTING_BUILD ? 225 : 0];
    private static final String LVL_V = "V";
    private static final String LVL_D = "D";
    private static final String LVL_YELL = "YELL";
    private static final String LVL_I = "I";
    private static final String LVL_W = "W";
    private static final String LVL_E = "E";
    private static final String LVL_WTF = "WTF";
    private static int msLogIndex = 0;
    @NonNull
    private static LogProvider msLogger = new LogCatLogProvider();

    private Logger() {
        //no instances please.
    }

    public static void setLogProvider(@NonNull LogProvider logProvider) {
        msLogger = logProvider;
    }

    private synchronized static void addLog(String level, String tag, String message) {
        if (BuildConfig.TESTING_BUILD) {
            msLogs[msLogIndex] = System.currentTimeMillis() + "-" + level + "-[" + tag + "] " + message;
            msLogIndex = (msLogIndex + 1) % msLogs.length;
        }
    }

    private synchronized static void addLog(String level, String tag, String message, Throwable t) {
        if (BuildConfig.TESTING_BUILD) {
            addLog(level, tag, message);
            addLog(level, tag, getStackTrace(t));
        }
    }

    @NonNull
    public synchronized static ArrayList<String> getAllLogLinesList() {
        ArrayList<String> lines = new ArrayList<>(msLogs.length);
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
        if (BuildConfig.TESTING_BUILD) {
            ArrayList<String> lines = getAllLogLinesList();
            //now to build the string
            StringBuilder sb = new StringBuilder("Log contains " + lines.size() + " lines:");
            while (lines.size() > 0) {
                String line = lines.remove(lines.size() - 1);
                sb.append(NEW_LINE);
                sb.append(line);
            }
            return sb.toString();
        } else {
            return "Not supported in RELEASE mode!";
        }
    }

    public static void v(String TAG, String text, Object... args) {
        if (BuildConfig.DEBUG) {
            String msg = args == null ? text : msFormatter.format(text, args).toString();
            msFormatBuilder.setLength(0);
            msLogger.v(TAG, msg);
            addLog(LVL_V, TAG, msg);
        }
    }

    public static void v(String TAG, String text, Throwable t) {
        if (BuildConfig.DEBUG) {
            msLogger.v(TAG, text + NEW_LINE + t);
            addLog(LVL_V, TAG, text, t);
        }
    }

    public static void d(String TAG, String text) {
        if (BuildConfig.TESTING_BUILD) {
            msLogger.d(TAG, text);
            addLog(LVL_D, TAG, text);
        }
    }

    public static void d(String TAG, String text, Object... args) {
        if (BuildConfig.TESTING_BUILD) {
            String msg = args == null ? text : msFormatter.format(text, args).toString();
            msFormatBuilder.setLength(0);
            msLogger.d(TAG, msg);
            addLog(LVL_D, TAG, msg);
        }
    }

    public static void d(String TAG, String text, Throwable t) {
        if (BuildConfig.TESTING_BUILD) {
            msLogger.d(TAG, text + NEW_LINE + t);
            addLog(LVL_D, TAG, text, t);
        }
    }

    public static void yell(String TAG, String text, Object... args) {
        if (BuildConfig.TESTING_BUILD) {
            String msg = args == null ? text : msFormatter.format(text, args).toString();
            msFormatBuilder.setLength(0);
            msLogger.yell(TAG, msg);
            addLog(LVL_YELL, TAG, msg);
        }
    }

    public static void i(String TAG, String text, Object... args) {
        String msg = args == null ? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        msLogger.i(TAG, msg);
        addLog(LVL_I, TAG, msg);
    }

    public static void i(String TAG, String text, Throwable t) {
        msLogger.i(TAG, text + NEW_LINE + t);
        addLog(LVL_I, TAG, text, t);
    }

    public static void w(String TAG, String text, Object... args) {
        String msg = args == null ? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        msLogger.w(TAG, msg);
        addLog(LVL_W, TAG, msg);
    }

    public static void w(String TAG, String text, Throwable t) {
        msLogger.w(TAG, text + NEW_LINE + t);
        addLog(LVL_W, TAG, text, t);
    }

    public static void e(String TAG, String text, Object... args) {
        String msg = args == null ? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        msLogger.e(TAG, msg);
        addLog(LVL_E, TAG, msg);
    }

    //TODO: remove this method
    public static void e(String TAG, String text, Throwable t) {
        msLogger.e(TAG, text + NEW_LINE + t);
        addLog(LVL_E, TAG, text, t);
    }

    public static void w(String TAG, Throwable e, String text, Object... args) {
        String msg = args == null ? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        msLogger.e(TAG, msg + NEW_LINE + e);
        addLog(LVL_E, TAG, msg);
    }

    public static void wtf(String TAG, String text, Object... args) {
        String msg = args == null ? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        addLog(LVL_WTF, TAG, msg);
        msLogger.wtf(TAG, msg);
    }

    public static void wtf(String TAG, String text, Throwable t) {
        addLog(LVL_WTF, TAG, text, t);
        msLogger.wtf(TAG, text + NEW_LINE + t);
    }

    public static String getStackTrace(Throwable ex) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        StringBuilder sb = new StringBuilder();

        for (StackTraceElement element : stackTrace) {
            sb.append("at ");//this is required for easy Proguard decoding.
            sb.append(element.toString());
            sb.append(NEW_LINE);
        }

        if (ex.getCause() == null)
            return sb.toString();
        else {
            ex = ex.getCause();
            String cause = getStackTrace(ex);
            sb.append("*** Cause: ").append(ex.getClass().getName());
            sb.append(NEW_LINE);
            sb.append("** Message: ").append(ex.getMessage());
            sb.append(NEW_LINE);
            sb.append("** Stack track: ").append(cause);
            sb.append(NEW_LINE);
            return sb.toString();
        }
    }
}
