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

package com.anysoftkeyboard.chewbacca;

import static com.anysoftkeyboard.base.utils.Logger.NEW_LINE;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.text.format.DateFormat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.anysoftkeyboard.base.utils.Logger;
import java.lang.Thread.UncaughtExceptionHandler;

public abstract class ChewbaccaUncaughtExceptionHandler implements UncaughtExceptionHandler {
    private static final String TAG = "ASKChewbacca";
    @NonNull protected final Context mApp;
    @Nullable private final UncaughtExceptionHandler mOsDefaultHandler;

    public ChewbaccaUncaughtExceptionHandler(
            @NonNull Context app, @Nullable UncaughtExceptionHandler previous) {
        mApp = app;
        mOsDefaultHandler = previous;
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, Throwable ex) {
        ex.printStackTrace();
        Logger.e(TAG, "Caught an unhandled exception!!!", ex);

        // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/15
        // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/433
        final String stackTrace = Logger.getStackTrace(ex);
        if (ex instanceof NullPointerException) {
            if (stackTrace.contains(
                            "android.inputmethodservice.IInputMethodSessionWrapper.executeMessage(IInputMethodSessionWrapper.java")
                    || stackTrace.contains(
                            "android.inputmethodservice.IInputMethodWrapper.executeMessage(IInputMethodWrapper.java")) {
                Logger.w(
                        TAG,
                        "An OS bug has been adverted. Move along, there is nothing to see here.");
                return;
            }
        } else if (ex instanceof java.util.concurrent.TimeoutException
                && stackTrace.contains(".finalize")) {
            Logger.w(TAG, "An OS bug has been adverted. Move along, there is nothing to see here.");
            return;
        }

        StringBuilder reportMessage = new StringBuilder();
        reportMessage
                .append("Hi. It seems that we have crashed.... Here are some details:")
                .append(NEW_LINE)
                .append("****** UTC Time: ")
                .append(DateFormat.format("kk:mm:ss dd.MM.yyyy", System.currentTimeMillis()))
                .append(NEW_LINE)
                .append("****** Application name: ")
                .append(getAppDetails())
                .append(NEW_LINE)
                .append("******************************")
                .append(NEW_LINE)
                .append(ex.getClass().getName())
                .append(NEW_LINE)
                .append("****** Exception message: ")
                .append(ex.getMessage())
                .append(NEW_LINE)
                .append("****** Trace trace:")
                .append(NEW_LINE)
                .append(stackTrace)
                .append(NEW_LINE)
                .append("******************************")
                .append(NEW_LINE)
                .append("****** Device information:")
                .append(NEW_LINE)
                .append(ChewbaccaUtils.getSysInfo(mApp))
                .append(NEW_LINE);
        if (ex instanceof OutOfMemoryError
                || (ex.getCause() != null && ex.getCause() instanceof OutOfMemoryError)) {
            reportMessage
                    .append("******************************")
                    .append(NEW_LINE)
                    .append("****** Memory: ")
                    .append(Runtime.getRuntime().totalMemory())
                    .append(NEW_LINE)
                    .append("Free: ")
                    .append(Runtime.getRuntime().freeMemory())
                    .append(NEW_LINE)
                    .append("Max: ")
                    .append(Runtime.getRuntime().maxMemory())
                    .append(NEW_LINE);
        }
        reportMessage
                .append("******************************")
                .append(NEW_LINE)
                .append("****** Log-Cat: ")
                .append(NEW_LINE)
                .append(Logger.getAllLogLines())
                .append(NEW_LINE);

        final Intent notificationIntent = createBugReportingActivityIntent();
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final Parcelable reportDetailsExtra = new BugReportDetails(ex, reportMessage.toString());
        notificationIntent.putExtra(
                BugReportDetails.EXTRA_KEY_BugReportDetails, reportDetailsExtra);

        final PendingIntent contentIntent =
                PendingIntent.getActivity(
                        mApp,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mApp, "Errors");
        builder.setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        setupNotification(builder, ex);

        // notifying
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mApp);
        notificationManager.notify(R.id.notification_icon_app_error, builder.build());

        // and sending to the OS
        if (mOsDefaultHandler != null) {
            Logger.i(TAG, "Sending the exception to OS exception handler...");
            mOsDefaultHandler.uncaughtException(thread, ex);
        }
    }

    @NonNull
    protected abstract Intent createBugReportingActivityIntent();

    protected abstract void setupNotification(
            @NonNull NotificationCompat.Builder builder, @NonNull Throwable ex);

    @NonNull
    protected abstract String getAppDetails();
}
