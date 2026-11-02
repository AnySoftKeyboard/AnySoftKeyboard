package com.anysoftkeyboard.saywhat;

import androidx.annotation.NonNull;
import com.menny.android.anysoftkeyboard.R;
import java.util.Calendar;
import java.util.Locale;

public class EarthDayNotice extends HolidayCardPublicNotice {

  public EarthDayNotice() {
    super(
        "HolidayEarthDay",
        new String[] {"earth", "nature"},
        "🌱 Happy Earth Day! 🌍",
        "🌱",
        "Happy Earth Day! 🌍🌱",
        R.string.public_notice_earth_day_title,
        R.string.public_notice_earth_day_message);
  }

  @Override
  protected boolean isDateActive(@NonNull Calendar now, @NonNull Locale locale) {
    return HolidayScheduleHelper.isEarthDayActive(now);
  }
}
