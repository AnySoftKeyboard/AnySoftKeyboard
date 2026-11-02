package com.anysoftkeyboard.saywhat;

import androidx.annotation.NonNull;
import com.menny.android.anysoftkeyboard.R;
import java.util.Calendar;
import java.util.Locale;

public class HalloweenNotice extends HolidayCardPublicNotice {

  public HalloweenNotice() {
    super(
        "HolidayHalloween",
        new String[] {"halloween", "spooky"},
        "🎃 Happy Halloween! 👻",
        "🎃",
        "Happy Halloween! 🎃👻",
        R.string.public_notice_halloween_title,
        R.string.public_notice_halloween_message);
  }

  @Override
  protected boolean isDateActive(@NonNull Calendar now, @NonNull Locale locale) {
    return HolidayScheduleHelper.isHalloweenActive(now, locale);
  }
}
