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

package com.anysoftkeyboard;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import com.anysoftkeyboard.ui.SendBugReportUiActivity;
import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

class ChewbaccaUncaughtExceptionHandler implements UncaughtExceptionHandler {
    private static final String TAG = "ASK CHEWBACCA";

    private final UncaughtExceptionHandler mOsDefaultHandler;

    private final Context mApp;

    public ChewbaccaUncaughtExceptionHandler(Context app,
                                             UncaughtExceptionHandler previous) {
        mApp = app;
        mOsDefaultHandler = previous;
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(TAG, "Caught an unhandled exception!!! ", ex);
        boolean ignore = false;

        // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/15
        String stackTrace = getStackTrace(ex);
        if (ex instanceof NullPointerException
                && stackTrace != null
                && stackTrace
                .contains("android.inputmethodservice.IInputMethodSessionWrapper.executeMessage(IInputMethodSessionWrapper.java")) {
            // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/15
            Log.w(TAG,
                    "An OS bug has been adverted. Move along, there is nothing to see here.");
            ignore = true;
        }

        if (!ignore && AnyApplication.getConfig().useChewbaccaNotifications()) {
            String appName = DeveloperUtils.getAppDetails(mApp
                    .getApplicationContext());

            final CharSequence utcTimeDate = DateFormat.format(
                    "kk:mm:ss dd.MM.yyyy", new Date());

            String logText = "Hi. It seems that we have crashed.... Here are some details:\n"
                    + "****** UTC Time: "
                    + utcTimeDate
                    + "\n"
                    + "****** Application name: "
                    + appName
                    + "\n"
                    + "******************************\n"
                    + "****** Exception type: "
                    + ex.getClass().getName()
                    + "\n"
                    + "****** Exception message: "
                    + ex.getMessage()
                    + "\n" + "****** Trace trace:\n" + stackTrace + "\n";
            logText += "******************************\n"
                    + "****** Device information:\n"
                    + DeveloperUtils.getSysInfo();
            if (ex instanceof OutOfMemoryError
                    || (ex.getCause() != null && ex.getCause() instanceof OutOfMemoryError)) {
                logText += "******************************\n"
                        + "****** Memory:\n" + getMemory();
            }
            logText += "******************************\n" + "****** Logcat:\n"
                    + getLogcat();

            Notification notification = new Notification(
                    R.drawable.notification_error_icon,
                    "Oops! Didn't see that coming, I crashed.",
                    System.currentTimeMillis());

            Intent notificationIntent = new Intent(mApp,
                    SendBugReportUiActivity.class);

            notificationIntent.putExtra(
                    SendBugReportUiActivity.CRASH_REPORT_TEXT, logText);

            PendingIntent contentIntent = PendingIntent.getActivity(mApp, 0,
                    notificationIntent, 0);

            notification.setLatestEventInfo(mApp,
                    mApp.getText(R.string.ime_name),
                    "Oops! Didn't see that coming, I crashed.", contentIntent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
            notification.defaults |= Notification.DEFAULT_LIGHTS;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            // notifying
            NotificationManager notificationManager = (NotificationManager) mApp
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1, notification);
        }
        // and sending to the OS
        if (!ignore && mOsDefaultHandler != null) {
            Log.i(TAG, "Sending the exception to OS exception handler...");
            mOsDefaultHandler.uncaughtException(thread, ex);
        }

        System.exit(0);
    }

    private String getLogcat() {
        return "Not supported at the moment";
    }

    private String getMemory() {
        String mem = "Total: " + Runtime.getRuntime().totalMemory() + "\n"
                + "Free: " + Runtime.getRuntime().freeMemory() + "\n" + "Max: "
                + Runtime.getRuntime().maxMemory() + "\n";

        if (AnyApplication.DEBUG) {
            try {
                File target = DeveloperUtils.createMemoryDump();
                mem += "Created hprof file at " + target.getAbsolutePath()
                        + "\n";
            } catch (Exception e) {
                mem += "Failed to create hprof file cause of " + e.getMessage();
                e.printStackTrace();
            }
        }

        return mem;
    }

    private String getStackTrace(Throwable ex) {
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
