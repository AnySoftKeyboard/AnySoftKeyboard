package com.anysoftkeyboard.saywhat;

import androidx.annotation.NonNull;
import java.util.Calendar;
import java.util.Locale;

public class PiDayNotice extends HolidayPublicNotice {

  public PiDayNotice() {
    super(
        "HolidayPiDay",
        new String[] {"3.14", "piday"},
        "π = 3.14159265...",
        "π",
        "π = 3.1415926535...");
  }

  @Override
  protected boolean isDateActive(@NonNull Calendar now, @NonNull Locale locale) {
    return HolidayScheduleHelper.isPiDayActive(now);
  }
}
