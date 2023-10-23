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

package com.anysoftkeyboard.releaseinfo;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.anysoftkeyboard.base.utils.CompatUtils;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.notification.NotificationDriver;
import com.anysoftkeyboard.notification.NotificationIds;
import com.anysoftkeyboard.prefs.DirectBootAwareSharedPreferences;
import java.util.Locale;

public class TesterNotification {
  private static final String TAG = "ASKTester";
  private static final String KEY = "testers_version_version_hash";

  public static void showDragonsIfNeeded(
      Context context, NotificationDriver notifier, boolean tester) {
    if (tester && firstTestersTimeVersionLoaded(context)) {
      Logger.i(TAG, "TESTERS VERSION added");

      PendingIntent contentIntent =
          PendingIntent.getActivity(
              context,
              0,
              new Intent(context, TestersNoticeActivity.class)
                  .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
              CompatUtils.appendImmutableFlag(0));

      var builder =
          notifier
              .buildNotification(
                  NotificationIds.Tester,
                  R.drawable.ic_notification_debug_version,
                  R.string.ime_name)
              .setContentText(context.getText(R.string.notification_text_testers))
              .setContentIntent(contentIntent)
              .setColor(
                  ContextCompat.getColor(context, R.color.notification_background_debug_version))
              .setAutoCancel(true);

      if (notifier.notify(builder, true)) {
        markTutorialShown(context);
      }
    }
  }

  private static String getPrefsValue(@NonNull Context context) {
    try {
      PackageInfo packageInfo =
          context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      var versionName = packageInfo.versionName;
      var versionCode = packageInfo.versionCode;
      return String.format(Locale.ROOT, "%s code %s", versionName, versionCode);
    } catch (PackageManager.NameNotFoundException e) {
      Logger.w(TAG, e, "Failed to get current package info.");
      return "err code err";
    }
  }

  private static boolean firstTestersTimeVersionLoaded(Context context) {
    SharedPreferences sp = DirectBootAwareSharedPreferences.create(context);
    final String lastDebugVersionHash = sp.getString(KEY, "NONE");
    return !getPrefsValue(context).equals(lastDebugVersionHash);
  }

  private static void markTutorialShown(Context context) {
    SharedPreferences sp = DirectBootAwareSharedPreferences.create(context);
    Editor e = sp.edit();
    e.putString(KEY, getPrefsValue(context));
    e.apply();
  }
}
