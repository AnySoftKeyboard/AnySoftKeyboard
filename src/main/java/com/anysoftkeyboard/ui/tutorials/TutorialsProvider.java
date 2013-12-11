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
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.FragmentChauffeurActivity;

import java.util.ArrayList;

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

    public static void showTips(Context context) {
        //starting the MainSettings activity, with TipsFragment
        Intent i = new Intent(context, MainSettingsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        FragmentChauffeurActivity.addIntentArgsForAddingFragmentToUi(i, TipsFragment.class, TipsFragment.createArgs(TipsFragment.SHOW_UNVIEWED_TIPS));
        context.startActivity(i);
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

    private static boolean firstTimeVersionChangeLogLoaded(Context context) {
        final String changeLogVersion = "last_changelog_ver_shown";
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final int lastTutorialVersion = sp.getInt(changeLogVersion, 0);
        final int packageVersion = getPackageVersion(context);

        Editor e = sp.edit();
        e.putInt(changeLogVersion, packageVersion);
        e.commit();

        return packageVersion != lastTutorialVersion;
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
        final NotificationManager mngr = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));

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
        mngr.notify(notificationData.NotificationID, notification);
    }

    public static boolean shouldShowTips(Context applicationContext) {
        //OK, I should show tips if there are some to show, but I don't want to annoy, so:
        //1) I wont show tips if the current version is old than 3 days - if the user hasn't read them so far..
        final long currentReleaseInstallTime = AnyApplication.getConfig().getTimeCurrentVersionInstalled();
        final long THREE_DAYS = 3*24*60*60*1000;
        if ((System.currentTimeMillis() - currentReleaseInstallTime) > THREE_DAYS) {
            //waited too long - NO TIPS FOR YOU!
            return false;
        }
        //let's see if there are new layouts for this user.
        ArrayList<Integer> layoutsToShow = new ArrayList<Integer>();
        SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        TipLayoutsSupport.getAvailableTipsLayouts(applicationContext, layoutsToShow);

        return layoutsToShow.size() > 0;
    }

}
