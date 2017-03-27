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

    private static synchronized void addLog(String level, String tag, String message) {
        if (BuildConfig.TESTING_BUILD) {
            msLogs[msLogIndex] = System.currentTimeMillis() + "-" + level + "-[" + tag + "] " + message;
            msLogIndex = (msLogIndex + 1) % msLogs.length;
        }
    }

    private static synchronized void addLog(String level, String tag, String message, Throwable t) {
        if (BuildConfig.TESTING_BUILD) {
            addLog(level, tag, message);
            addLog(level, tag, getStackTrace(t));
        }
    }

    @NonNull
    public static synchronized ArrayList<String> getAllLogLinesList() {
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
    public static synchronized String getAllLogLines() {
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

    public static synchronized void v(final String tag, String text, Object... args) {
        if (BuildConfig.DEBUG) {
            String msg = args == null ? text : msFormatter.format(text, args).toString();
            msFormatBuilder.setLength(0);
            msLogger.v(tag, msg);
            addLog(LVL_V, tag, msg);
        }
    }

    public static synchronized void v(final String tag, String text, Throwable t) {
        if (BuildConfig.DEBUG) {
            msLogger.v(tag, text + NEW_LINE + t);
            addLog(LVL_V, tag, text, t);
        }
    }

    public static synchronized void d(final String tag, String text) {
        if (BuildConfig.TESTING_BUILD) {
            msLogger.d(tag, text);
            addLog(LVL_D, tag, text);
        }
    }

    public static synchronized void d(final String tag, String text, Object... args) {
        if (BuildConfig.TESTING_BUILD) {
            String msg = args == null ? text : msFormatter.format(text, args).toString();
            msFormatBuilder.setLength(0);
            msLogger.d(tag, msg);
            addLog(LVL_D, tag, msg);
        }
    }

    public static synchronized void d(final String tag, String text, Throwable t) {
        if (BuildConfig.TESTING_BUILD) {
            msLogger.d(tag, text + NEW_LINE + t);
            addLog(LVL_D, tag, text, t);
        }
    }

    public static synchronized void yell(final String tag, String text, Object... args) {
        if (BuildConfig.TESTING_BUILD) {
            String msg = args == null ? text : msFormatter.format(text, args).toString();
            msFormatBuilder.setLength(0);
            msLogger.yell(tag, msg);
            addLog(LVL_YELL, tag, msg);
        }
    }

    public static synchronized void i(final String tag, String text, Object... args) {
        String msg = args == null ? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        msLogger.i(tag, msg);
        addLog(LVL_I, tag, msg);
    }

    public static synchronized void i(final String tag, String text, Throwable t) {
        msLogger.i(tag, text + NEW_LINE + t);
        addLog(LVL_I, tag, text, t);
    }

    public static synchronized void w(final String tag, String text, Object... args) {
        String msg = args == null ? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        msLogger.w(tag, msg);
        addLog(LVL_W, tag, msg);
    }

    public static synchronized void w(final String tag, String text, Throwable t) {
        msLogger.w(tag, text + NEW_LINE + t);
        addLog(LVL_W, tag, text, t);
    }

    public static synchronized void e(final String tag, String text, Object... args) {
        String msg = args == null ? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        msLogger.e(tag, msg);
        addLog(LVL_E, tag, msg);
    }

    //TODO: remove this method
    public static synchronized void e(final String tag, String text, Throwable t) {
        msLogger.e(tag, text + NEW_LINE + t);
        addLog(LVL_E, tag, text, t);
    }

    public static synchronized void w(final String tag, Throwable e, String text, Object... args) {
        String msg = args == null ? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        msLogger.e(tag, msg + NEW_LINE + e);
        addLog(LVL_E, tag, msg);
    }

    public static synchronized void wtf(final String tag, String text, Object... args) {
        String msg = args == null ? text : msFormatter.format(text, args).toString();
        msFormatBuilder.setLength(0);
        addLog(LVL_WTF, tag, msg);
        msLogger.wtf(tag, msg);
    }

    public static synchronized void wtf(final String tag, String text, Throwable t) {
        addLog(LVL_WTF, tag, text, t);
        msLogger.wtf(tag, text + NEW_LINE + t);
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
