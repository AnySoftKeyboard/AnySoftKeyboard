package com.anysoftkeyboard.saywhat;

import androidx.annotation.NonNull;
import java.util.Calendar;
import java.util.Locale;

public class StarWarsDayNotice extends HolidayPublicNotice {

  public StarWarsDayNotice() {
    super(
        "HolidayStarWarsDay",
        new String[] {"may the 4th", "star wars"},
        "✨ May the Force be with you! ⚔️",
        "⚔️",
        "May the 4th be with you! ✨");
  }

  @Override
  protected boolean isDateActive(@NonNull Calendar now, @NonNull Locale locale) {
    return HolidayScheduleHelper.isStarWarsDayActive(now);
  }
}
