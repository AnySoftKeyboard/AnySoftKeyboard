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
import android.content.res.Resources;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.ui.SendBugReportUiActivity;
import com.anysoftkeyboard.ui.dev.DeveloperUtils;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.functions.Consumer;

class ChewbaccaUncaughtExceptionHandler implements UncaughtExceptionHandler, Consumer<Throwable> {
    private static final String TAG = "ASK CHEWBACCA";

    private final UncaughtExceptionHandler mOsDefaultHandler;
    private final Context mApp;

    public ChewbaccaUncaughtExceptionHandler(Context app, UncaughtExceptionHandler previous) {
        mApp = app;
        mOsDefaultHandler = previous;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        Logger.e(TAG, "Caught an unhandled exception!!!", ex);

        // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/15
        //https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/433
        String stackTrace = Logger.getStackTrace(ex);
        if (ex instanceof NullPointerException) {
            if (stackTrace.contains("android.inputmethodservice.IInputMethodSessionWrapper.executeMessage(IInputMethodSessionWrapper.java") ||
                    stackTrace.contains("android.inputmethodservice.IInputMethodWrapper.executeMessage(IInputMethodWrapper.java")) {
                Logger.w(TAG, "An OS bug has been adverted. Move along, there is nothing to see here.");
                return;
            }
        } else if (ex instanceof java.util.concurrent.TimeoutException && stackTrace.contains(".finalize")) {
            Logger.w(TAG, "An OS bug has been adverted. Move along, there is nothing to see here.");
            return;
        }

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
                + DeveloperUtils.getSysInfo(mApp);
        if (ex instanceof OutOfMemoryError
                || (ex.getCause() != null && ex.getCause() instanceof OutOfMemoryError)) {
            logText += "******************************\n"
                    + "****** Memory:" + newline + getMemory();
        }

        if (ex instanceof Resources.NotFoundException) {
            int resourceId = extractResourceIdFromException((Resources.NotFoundException) ex);
            logText += "******************************\n";
            if (resourceId == 0) {
                logText += "Failed to extract resource id from message\n";
            } else {
                String possibleResources = getResourcesNamesWithValue(resourceId);
                if (TextUtils.isEmpty(possibleResources)) {
                    logText += "Could not find matching resources for resource id " + resourceId + ", this may happen if the resource is from an external package.\n";
                } else {
                    logText += "Possible resources for " + resourceId + ":\n";
                }

            }
            logText += "******************************\n";
        }
        logText += "******************************" + newline + "****** Log-Cat:" + newline
                + Logger.getAllLogLines();

        String crashType = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        Intent notificationIntent = new Intent(mApp, SendBugReportUiActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final Parcelable reportDetailsExtra = new SendBugReportUiActivity.BugReportDetails(ex, logText);
        notificationIntent.putExtra(SendBugReportUiActivity.EXTRA_KEY_BugReportDetails, reportDetailsExtra);

        PendingIntent contentIntent = PendingIntent.getActivity(mApp, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mApp);
        builder.setSmallIcon(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ?
                R.drawable.notification_error_icon : R.drawable.ic_notification_error)
                .setColor(ContextCompat.getColor(mApp, R.color.notification_background_error))
                .setTicker(mApp.getText(R.string.ime_crashed_ticker))
                .setContentTitle(mApp.getText(R.string.ime_name))
                .setContentText(mApp.getText(R.string.ime_crashed_sub_text))
                .setSubText(BuildConfig.TESTING_BUILD ? crashType : null/*not showing the type of crash in RELEASE mode*/)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);

        // notifying
        NotificationManager notificationManager =
                (NotificationManager) mApp.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(R.id.notification_icon_app_error, builder.build());

        // and sending to the OS
        if (mOsDefaultHandler != null) {
            Logger.i(TAG, "Sending the exception to OS exception handler...");
            mOsDefaultHandler.uncaughtException(thread, ex);
        }
    }

    private String getResourcesNamesWithValue(int resourceId) {
        StringBuilder resources = new StringBuilder();
        addResourceNameWithId(resources, resourceId, R.anim.class);
        addResourceNameWithId(resources, resourceId, R.array.class);
        addResourceNameWithId(resources, resourceId, R.attr.class);
        addResourceNameWithId(resources, resourceId, R.bool.class);
        addResourceNameWithId(resources, resourceId, R.color.class);
        addResourceNameWithId(resources, resourceId, R.dimen.class);
        addResourceNameWithId(resources, resourceId, R.drawable.class);
        addResourceNameWithId(resources, resourceId, R.id.class);
        addResourceNameWithId(resources, resourceId, R.integer.class);
        addResourceNameWithId(resources, resourceId, R.layout.class);
        addResourceNameWithId(resources, resourceId, R.menu.class);
        addResourceNameWithId(resources, resourceId, R.mipmap.class);
        addResourceNameWithId(resources, resourceId, R.raw.class);
        addResourceNameWithId(resources, resourceId, R.string.class);
        addResourceNameWithId(resources, resourceId, R.style.class);
        addResourceNameWithId(resources, resourceId, R.styleable.class);
        addResourceNameWithId(resources, resourceId, R.xml.class);

        return resources.toString();
    }

    private void addResourceNameWithId(StringBuilder resources, int resourceId, Class<?> clazz) {
        for (Field field : clazz.getFields()) {
            if (field.getType().equals(int.class) && (field.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) != 0) {
                try {
                    if (resourceId == field.getInt(null)) {
                        resources.append(clazz.getName()).append(".").append(field.getName());
                        resources.append('\n');
                    }
                } catch (IllegalAccessException e) {
                    Logger.d("EEEE", "Failed to access " + field.getName(), e);
                }
            }
        }
    }

    private int extractResourceIdFromException(Resources.NotFoundException ex) {
        try {
            String message = ex.getMessage();
            if (TextUtils.isEmpty(message)) return 0;

            Pattern pattern = Pattern.compile("#0x([0-9a-fA-F]+)");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                String hexValue = matcher.group(1);
                return Integer.parseInt(hexValue.trim(), 16);
            } else {
                return 0;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    private String getMemory() {
        String mem = "Total: " + Runtime.getRuntime().totalMemory() + "\n"
                + "Free: " + Runtime.getRuntime().freeMemory() + "\n" + "Max: "
                + Runtime.getRuntime().maxMemory() + "\n";

        if (BuildConfig.TESTING_BUILD) {
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

    @Override
    public void accept(Throwable throwable) throws Exception {
        uncaughtException(Thread.currentThread(), throwable);
    }
}
