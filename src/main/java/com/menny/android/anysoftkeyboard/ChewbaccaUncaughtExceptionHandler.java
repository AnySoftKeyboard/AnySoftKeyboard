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

package com.menny.android.anysoftkeyboard;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;

import com.anysoftkeyboard.ui.SendBugReportUiActivity;
import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.anysoftkeyboard.utils.Log;

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
        String stackTrace = Log.getStackTrace(ex);
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
            String appName = DeveloperUtils.getAppDetails(mApp);

            final CharSequence utcTimeDate = DateFormat.format(
                    "kk:mm:ss dd.MM.yyyy", new Date());
            final String newline = DeveloperUtils.NEW_LINE;
            String logText = "Hi. It seems that we have crashed.... Here are some details:" + newline
                    + "****** UTC Time: "
                    + utcTimeDate
                    + newline
                    + "****** Application name: "
                    + appName
                    + newline
                    + "******************************" + newline
                    + "****** Exception type: "
                    + ex.getClass().getName()
                    + newline
                    + "****** Exception message: "
                    + ex.getMessage()
                    + newline + "****** Trace trace:" + newline + stackTrace + newline;
            logText += "******************************" + newline
                    + "****** Device information:" + newline
                    + DeveloperUtils.getSysInfo();
            if (ex instanceof OutOfMemoryError
                    || (ex.getCause() != null && ex.getCause() instanceof OutOfMemoryError)) {
                logText += "******************************\n"
                        + "****** Memory:" + newline + getMemory();
            }
            logText += "******************************" + newline + "****** Log-Cat:" + newline
                    + Log.getAllLogLines();

            String crashType = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            Intent notificationIntent = new Intent(mApp, SendBugReportUiActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            notificationIntent.putExtra(SendBugReportUiActivity.EXTRA_KEY_BugReportDetails,
                    (Parcelable) new SendBugReportUiActivity.BugReportDetails(ex, logText));

            PendingIntent contentIntent = PendingIntent.getActivity(mApp, 0,
                    notificationIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mApp);
            builder.setSmallIcon(R.drawable.notification_error_icon).
                    setTicker(mApp.getText(R.string.ime_crashed_ticker)).
                    setContentTitle(mApp.getText(R.string.ime_name)).
                    setContentText(mApp.getText(R.string.ime_crashed_sub_text)).
                    setSubText(BuildConfig.DEBUG ? crashType : null/*not showing the type of crash in RELEASE mode*/).
                    setWhen(System.currentTimeMillis()).
                    setContentIntent(contentIntent).
                    setAutoCancel(true).
                    setOnlyAlertOnce(true).
                    setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);

            // notifying
            NotificationManager notificationManager = (NotificationManager) mApp
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1, builder.build());
        }
        // and sending to the OS
        if (!ignore && mOsDefaultHandler != null) {
            Log.i(TAG, "Sending the exception to OS exception handler...");
            mOsDefaultHandler.uncaughtException(thread, ex);
        }

        Thread.yield();
        //halting the process. No need to continue now. I'm a dead duck.
        System.exit(0);
    }

    private String getMemory() {
        String mem = "Total: " + Runtime.getRuntime().totalMemory() + "\n"
                + "Free: " + Runtime.getRuntime().freeMemory() + "\n" + "Max: "
                + Runtime.getRuntime().maxMemory() + "\n";

        if (BuildConfig.DEBUG) {
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
}
