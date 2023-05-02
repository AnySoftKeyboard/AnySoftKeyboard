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
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateFormat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.anysoftkeyboard.base.utils.CompatUtils;
import com.anysoftkeyboard.base.utils.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;

public abstract class ChewbaccaUncaughtExceptionHandler implements UncaughtExceptionHandler {
    @VisibleForTesting static final String NEW_CRASH_FILENAME = "new_crash_details.log";

    @VisibleForTesting
    static final String ACK_CRASH_FILENAME_TEMPLATE = "crash_report_details_{TIME}.log";

    @VisibleForTesting static final String HEADER_BREAK_LINE = "----- FULL REPORT -----";

    private static final String TAG = "ASKChewbacca";
    @NonNull protected final Context mApp;
    @Nullable private final UncaughtExceptionHandler mOsDefaultHandler;

    public ChewbaccaUncaughtExceptionHandler(
            @NonNull Context app, @Nullable UncaughtExceptionHandler previous) {
        mApp = app;
        mOsDefaultHandler = previous;
    }

    private static String getAckReportFilename() {
        return ACK_CRASH_FILENAME_TEMPLATE.replace(
                "{TIME}", Long.toString(System.currentTimeMillis()));
    }

    public boolean performCrashDetectingFlow() {
        final File newCrashFile = new File(mApp.getFilesDir(), NEW_CRASH_FILENAME);
        if (newCrashFile.isFile()) {
            String ackReportFilename = getAckReportFilename();
            StringBuilder header = new StringBuilder();
            StringBuilder report = new StringBuilder();
            try (BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    mApp.openFileInput(NEW_CRASH_FILENAME),
                                    Charset.forName("UTF-8")))) {
                try (BufferedWriter writer =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        mApp.openFileOutput(
                                                ackReportFilename, Context.MODE_PRIVATE),
                                        Charset.forName("UTF-8")))) {
                    Logger.i(TAG, "Archiving crash report to %s.", ackReportFilename);
                    Logger.d(TAG, "Crash report:");
                    String line;
                    boolean stillInHeader = true;
                    while (null != (line = reader.readLine())) {
                        writer.write(line);
                        writer.newLine();
                        report.append(line).append(NEW_LINE);
                        if (line.equals(HEADER_BREAK_LINE)) stillInHeader = false;
                        if (stillInHeader) header.append(line).append(NEW_LINE);
                        Logger.d(TAG, "err: %s", line);
                    }
                }
            } catch (Exception e) {
                Logger.e(TAG, "Failed to write crash report to archive!");
                return false;
            }

            if (!newCrashFile.delete()) {
                Logger.e(TAG, "Failed to delete crash log! %s", newCrashFile.getAbsolutePath());
            }

            sendNotification(
                    header.toString(),
                    report.toString(),
                    new File(mApp.getFilesDir(), ackReportFilename));

            return true;
        }
        return false;
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
                .append(HEADER_BREAK_LINE)
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

        try (OutputStreamWriter writer =
                new OutputStreamWriter(
                        mApp.openFileOutput(NEW_CRASH_FILENAME, Context.MODE_PRIVATE),
                        Charset.forName("UTF-8"))) {
            writer.write(reportMessage.toString());
            Logger.i(TAG, "Wrote crash report to %s.", NEW_CRASH_FILENAME);
            Logger.d(TAG, "Crash report:");
            for (String line : TextUtils.split(reportMessage.toString(), NEW_LINE)) {
                Logger.d(TAG, "err: %s", line);
            }
        } catch (Exception writeEx) {
            Logger.e(TAG, writeEx, "Failed to write crash report file!");
        }
        // and sending to the OS
        if (mOsDefaultHandler != null) {
            Logger.i(TAG, "Sending the exception to OS exception handler...");
            mOsDefaultHandler.uncaughtException(thread, ex);
        }
    }

    private void sendNotification(
            @NonNull String reportHeader, @NonNull String crashReport, @NonNull File reportFile) {
        final Intent notificationIntent = createBugReportingActivityIntent();
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.putExtra(
                BugReportDetails.EXTRA_KEY_BugReportDetails,
                new BugReportDetails(reportHeader, crashReport, Uri.fromFile(reportFile)));

        final PendingIntent contentIntent =
                PendingIntent.getActivity(
                        mApp,
                        0,
                        notificationIntent,
                        CompatUtils.appendImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationChannelCompat notificationChannel =
                new NotificationChannelCompat.Builder(
                                "crash", NotificationManagerCompat.IMPORTANCE_HIGH)
                        .setName("App Crash Report")
                        .setLightsEnabled(true)
                        .setLightsEnabled(true)
                        .build();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mApp, notificationChannel.getId());
        builder.setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(false);
        setupNotification(builder);

        // notifying
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mApp);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(R.id.notification_icon_app_error, builder.build());
    }

    @NonNull protected abstract Intent createBugReportingActivityIntent();

    protected abstract void setupNotification(@NonNull NotificationCompat.Builder builder);

    @NonNull protected abstract String getAppDetails();
}
