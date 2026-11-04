package com.anysoftkeyboard.saywhat;

import androidx.annotation.NonNull;
import com.menny.android.anysoftkeyboard.R;
import java.util.Calendar;
import java.util.Locale;

public class PrivacyDayNotice extends HolidayCardPublicNotice {

  public PrivacyDayNotice() {
    super(
        "HolidayWorldPrivacyDay",
        new String[] {"privacy", "encrypt"},
        "🛡️ Protect Your Privacy! 🔒",
        "🛡️",
        "Global Encryption & World Privacy Day 🛡️",
        R.string.public_notice_privacy_day_title,
        R.string.public_notice_privacy_day_message);
  }

  @Override
  protected boolean isDateActive(@NonNull Calendar now, @NonNull Locale locale) {
    return HolidayScheduleHelper.isPrivacyDayActive(now);
  }
}
