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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;

import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

public class TutorialsProvider {
    private static final String TAG = "ASK Tutorial";

    public static void showDragonsIfNeeded(Context context) {
        if (BuildConfig.TESTING_BUILD && firstTestersTimeVersionLoaded(context)) {
            Logger.i(TAG, "TESTERS VERSION added");

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, TestersNoticeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);

            final NotificationManager manager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
            notificationBuilder.setSmallIcon(
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ?
                            R.drawable.notification_icon_beta_version : R.drawable.ic_notification_debug_version)
                    .setContentText(context.getText(R.string.notification_text_testers))
                    .setContentTitle(context.getText(R.string.ime_name_beta))
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .setColor(ContextCompat.getColor(context, R.color.notification_background_debug_version))
                    .setDefaults(0/*no sound, vibrate, etc*/)
                    .setAutoCancel(true);

            manager.notify(R.id.notification_icon_debug_version, notificationBuilder.build());
        }
    }

    private static boolean firstTestersTimeVersionLoaded(Context context) {
        final String KEY = "testers_version_version_hash";
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final String lastDebugVersionHash = sp.getString(KEY, "NONE");
        String currentHash = BuildConfig.VERSION_NAME + " code " + BuildConfig.VERSION_CODE;

        Editor e = sp.edit();
        e.putString(KEY, currentHash);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(e);

        return !currentHash.equals(lastDebugVersionHash);
    }
}
