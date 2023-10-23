package com.anysoftkeyboard.notification;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.permissions.PermissionRequestHelper;

public class NotificationDriverImpl implements NotificationDriver {

  @NonNull private final Context mContext;
  @NonNull private final NotificationManagerCompat mManager;
  private boolean mInitialized = false;

  public NotificationDriverImpl(@NonNull Context context) {
    mContext = context;
    mManager = NotificationManagerCompat.from(mContext);
  }

  @Override
  public void initializeChannels(boolean isProduction) {
    if (mInitialized) throw new RuntimeException("Already initialized");

    for (NotificationChannels channel : NotificationChannels.values()) {
      if (isProduction && !channel.mIsProduction) continue;

      var notificationChannel =
          new NotificationChannelCompat.Builder(channel.mChannelId, channel.mImportance)
              .setName(channel.mChannelId)
              .setDescription(channel.mDescription)
              .setLightsEnabled(true)
              .setVibrationEnabled(true)
              .build();

      mManager.createNotificationChannel(notificationChannel);
    }
    mInitialized = true;
  }

  @Override
  public NotifyBuilder buildNotification(
      @NonNull NotificationIds notificationId, @DrawableRes int icon, @StringRes int title) {
    return (NotifyBuilder)
        new NotifyBuilder(mContext, notificationId)
            .setSmallIcon(icon)
            .setContentTitle(mContext.getText(title))
            .setDefaults(0 /*no sound, vibrate, etc*/)
            .setWhen(System.currentTimeMillis());
  }

  @Override
  public boolean notify(@NonNull NotificationCompat.Builder builder, boolean skipIfNoPermission) {
    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED) {
      if (!skipIfNoPermission) {
        Logger.w(
            "NotificationDriverImpl",
            "Can not send notification since POST_NOTIFICATIONS is not granted.");
      }
      return false;
    } else {
      NotifyBuilder myBuilder = (NotifyBuilder) builder;
      mManager.notify(myBuilder.mNotificationId.mNotificationId, myBuilder.build());
      return true;
    }
  }

  @Override
  public boolean askForNotificationPostPermission(@NonNull Activity activity) {
    return PermissionRequestHelper.check(
        activity, PermissionRequestHelper.NOTIFICATION_PERMISSION_REQUEST_CODE);
  }

  @Override
  public boolean askForNotificationPostPermission(@NonNull Fragment fragment) {
    return PermissionRequestHelper.check(
        fragment, PermissionRequestHelper.NOTIFICATION_PERMISSION_REQUEST_CODE);
  }
}
