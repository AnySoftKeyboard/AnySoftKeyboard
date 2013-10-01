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

package com.anysoftkeyboard.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.anysoftkeyboard.ui.tutorials.WelcomeHowToNoticeActivity;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

/*
 * This receiver is (hopefully) called after AnySoftKeyboard installed from the Market
 * see:
 * http://code.google.com/mobile/analytics/docs/android/#referrals
 * http://stackoverflow.com/questions/4093150/get-referrer-after-installing-app-from-android-market
 * 
 * In Market v3+ this will not happen anymore :(
 */
public class AnySoftKeyboardInstalledReceiver extends BroadcastReceiver {

    private static final String TAG = "ASK Installed";
    public static final int INSTALLED_NOTIFICATION_ID = 45711;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "Thank you for installing AnySoftKeyboard! We hope you'll like it.");

        if (WelcomeHowToNoticeActivity.shouldShowWelcomeActivity(context)) {
            Intent i = new Intent(context, WelcomeHowToNoticeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            final NotificationManager mngr = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));

            Notification notification = new Notification(R.drawable.notification_icon_how_to, context.getText(R.string.notification_title_how_to_enable), System.currentTimeMillis());

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, 0);

            notification.setLatestEventInfo(context,
                    context.getText(R.string.notification_title_how_to_enable),
                    context.getText(R.string.notification_text_how_to_enable),
                    contentIntent);
            notification.defaults = 0;// no sound, vibrate, etc.
            //Cancel on click
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            // notifying
            //need different id for each notification, so we can cancel easily
            mngr.notify(INSTALLED_NOTIFICATION_ID, notification);
        }
    }
}