package com.anysoftkeyboard.notification;

import androidx.annotation.NonNull;

public enum NotificationIds {
  Tester(NotificationChannels.Tester, 1),
  CrashDetected(NotificationChannels.Crash, 2),
  RequestContactsPermission(NotificationChannels.Permissions, 3);

  final NotificationChannels mChannel;
  final int mNotificationId;

  NotificationIds(@NonNull NotificationChannels channel, int id) {
    mChannel = channel;
    mNotificationId = id;
  }
}
