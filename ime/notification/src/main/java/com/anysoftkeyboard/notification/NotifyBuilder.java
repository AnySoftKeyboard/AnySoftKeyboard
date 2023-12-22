package com.anysoftkeyboard.notification;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class NotifyBuilder extends NotificationCompat.Builder {
  final NotificationIds mNotificationId;

  NotifyBuilder(@NonNull Context context, @NonNull NotificationIds id) {
    super(context, id.mChannel.mChannelId);
    mNotificationId = id;
  }
}
