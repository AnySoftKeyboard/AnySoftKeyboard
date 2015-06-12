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

package com.anysoftkeyboard.ui.tutorials;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

public class TutorialsProvider {
    //public static final String TUTORIALS_SP_FILENAME = "tutorials";

    private static final String TAG = "ASK Turorial";

    private static final int TUTORIALS_NOTIFICATION_ID_BASE = 102431;


    public static void showDragonsIfNeeded(Context context) {
        if (BuildConfig.DEBUG && firstTestersTimeVersionLoaded(context)) {
            Log.i(TAG, "TESTERS VERSION added");

            Intent i = new Intent(context, TestersNoticeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            showNotificationIcon(context, new IntentToLaunch(
                    TUTORIALS_NOTIFICATION_ID_BASE + 1, i, R.drawable.notification_icon_beta_version,
                    R.string.ime_name_beta, R.string.notification_text_testers));
        }
    }

    private static boolean firstTestersTimeVersionLoaded(Context context) {
        final String KEY = "testers_version_version_hash";
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final String lastDebugVersionHash = sp.getString(KEY, "NONE");
        String currentHash = "";
        try {
            PackageInfo pi = DeveloperUtils.getPackageInfo(context);
            currentHash = pi.versionName + " code " + pi.versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Editor e = sp.edit();
        e.putString(KEY, currentHash);
        e.commit();

        return !currentHash.equals(lastDebugVersionHash);
    }

    public static int getPackageVersion(Context context) {
        try {
            PackageInfo pi = DeveloperUtils.getPackageInfo(context);
            return pi.versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
    }

    public synchronized static void showNotificationIcon(Context context, IntentToLaunch notificationData) {
        final NotificationManager manager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));

        Notification notification = new Notification(notificationData.NotificationIcon, context.getText(notificationData.NotificationText), System.currentTimeMillis());

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationData.IntentToStart, 0);

        notification.setLatestEventInfo(context,
                context.getText(notificationData.NotificationTitle), context.getText(notificationData.NotificationText),
                contentIntent);
        notification.defaults = 0;// no sound, vibrate, etc.
        //Cancel on click
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        // notifying
        //need different id for each notification, so we can cancel easily
        manager.notify(notificationData.NotificationID, notification);
    }

}
