package com.anysoftkeyboard.notification;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

public enum NotificationChannels {
  Tester("Tester", "You are a tester!", false, NotificationManagerCompat.IMPORTANCE_HIGH),
  Crash("Crash", "Crash reporting", true, NotificationManagerCompat.IMPORTANCE_HIGH),
  Permissions(
      "Permissions",
      "Request access permission",
      true,
      NotificationManagerCompat.IMPORTANCE_DEFAULT);

  final String mChannelId;
  final String mDescription;
  final boolean mIsProduction;
  final int mImportance;

  NotificationChannels(
      @NonNull String channelId,
      @NonNull String description,
      boolean isProduction,
      int importance) {
    mChannelId = channelId;
    mDescription = description;
    mIsProduction = isProduction;
    mImportance = importance;
  }
}
