package com.anysoftkeyboard.saywhat;

import androidx.annotation.NonNull;
import com.menny.android.anysoftkeyboard.R;
import java.util.Calendar;
import java.util.Locale;

public class ThanksgivingNotice extends HolidayCardPublicNotice {

  public ThanksgivingNotice() {
    super(
        "HolidayThanksgiving",
        new String[] {"thanksgiving", "turkey"},
        "🦃 Happy Thanksgiving! 🍂",
        "🦃",
        "Happy Thanksgiving! 🦃",
        R.string.public_notice_thanksgiving_title,
        R.string.public_notice_thanksgiving_message);
  }

  @Override
  protected boolean isDateActive(@NonNull Calendar now, @NonNull Locale locale) {
    return HolidayScheduleHelper.isThanksgivingActive(now, locale);
  }
}
