package com.anysoftkeyboard.saywhat;

import androidx.annotation.NonNull;
import com.menny.android.anysoftkeyboard.R;
import java.util.Calendar;
import java.util.Locale;

public class NewYearsDayNotice extends HolidayCardPublicNotice {

  public NewYearsDayNotice() {
    super(
        "HolidayNewYearsDay",
        new String[] {"happy new year", "january 1"},
        "🎉 Happy New Year! ✨",
        "🎉",
        "Happy New Year! 🎉",
        R.string.public_notice_new_year_title,
        R.string.public_notice_new_year_message);
  }

  @Override
  protected boolean isDateActive(@NonNull Calendar now, @NonNull Locale locale) {
    return HolidayScheduleHelper.isNewYearsDayKeyboardActive(now);
  }

  @Override
  protected boolean isUiActive(@NonNull Calendar now, @NonNull Locale locale) {
    return HolidayScheduleHelper.isNewYearsDayUiActive(now);
  }
}
