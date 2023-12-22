package com.anysoftkeyboard.notification;

import android.app.Activity;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

public interface NotificationDriver {

  void initializeChannels(boolean isProduction);

  NotifyBuilder buildNotification(
      @NonNull NotificationIds notificationId, @DrawableRes int icon, @StringRes int title);

  boolean notify(@NonNull NotificationCompat.Builder builder, boolean skipIfNoPermission);

  boolean askForNotificationPostPermission(@NonNull Activity activity);

  boolean askForNotificationPostPermission(@NonNull Fragment fragment);
}
