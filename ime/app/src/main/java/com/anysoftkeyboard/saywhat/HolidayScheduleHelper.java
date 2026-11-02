package com.anysoftkeyboard.saywhat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Calendar;
import java.util.Locale;

public final class HolidayScheduleHelper {

  private HolidayScheduleHelper() {}

  /**
   * Thanksgiving (US only): 4th Thursday of November, active 1 week before through the day of.
   */
  public static boolean isThanksgivingActive(@NonNull Calendar calendar, @Nullable Locale locale) {
    if (locale == null || !"US".equalsIgnoreCase(locale.getCountry())) {
      return false;
    }
    if (calendar.get(Calendar.MONTH) != Calendar.NOVEMBER) {
      return false;
    }
    int fourthThursdayDay = getThanksgivingDayOfMonth(calendar.get(Calendar.YEAR));
    int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
    return currentDay >= (fourthThursdayDay - 7) && currentDay <= fourthThursdayDay;
  }

  /**
   * Calculates the day of the month for Thanksgiving (4th Thursday of November) in the given year.
   */
  public static int getThanksgivingDayOfMonth(int year) {
    Calendar nov1 = Calendar.getInstance();
    nov1.clear();
    nov1.set(year, Calendar.NOVEMBER, 1);
    int dayOfWeek = nov1.get(Calendar.DAY_OF_WEEK);
    int daysUntilThursday = (Calendar.THURSDAY - dayOfWeek + 7) % 7;
    int firstThursday = 1 + daysUntilThursday;
    return firstThursday + 21;
  }

  /**
   * Halloween: Oct 29 through Oct 31 (inclusive).
   * Observing locales: US, CA, GB, IE, AU, NZ, MX.
   */
  public static boolean isHalloweenActive(@NonNull Calendar calendar, @Nullable Locale locale) {
    if (locale == null || !isHalloweenObservingLocale(locale.getCountry())) {
      return false;
    }
    if (calendar.get(Calendar.MONTH) != Calendar.OCTOBER) {
      return false;
    }
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    return day >= 29 && day <= 31;
  }

  public static boolean isHalloweenObservingLocale(@Nullable String countryCode) {
    if (countryCode == null) return false;
    String country = countryCode.toUpperCase(Locale.US);
    return country.equals("US")
        || country.equals("CA")
        || country.equals("GB")
        || country.equals("IE")
        || country.equals("AU")
        || country.equals("NZ")
        || country.equals("MX");
  }

  /**
   * New Year's Eve: Dec 31, between 21:00 and 23:59:59.
   */
  public static boolean isNewYearsEveActive(@NonNull Calendar calendar) {
    if (calendar.get(Calendar.MONTH) != Calendar.DECEMBER) {
      return false;
    }
    if (calendar.get(Calendar.DAY_OF_MONTH) != 31) {
      return false;
    }
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    return hour >= 21;
  }

  /**
   * New Year's Day (Keyboard celebration): Jan 1.
   */
  public static boolean isNewYearsDayKeyboardActive(@NonNull Calendar calendar) {
    return calendar.get(Calendar.MONTH) == Calendar.JANUARY
        && calendar.get(Calendar.DAY_OF_MONTH) == 1;
  }

  /**
   * New Year's Day (UI Card): Jan 1 through Jan 3 (inclusive).
   */
  public static boolean isNewYearsDayUiActive(@NonNull Calendar calendar) {
    if (calendar.get(Calendar.MONTH) != Calendar.JANUARY) {
      return false;
    }
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    return day >= 1 && day <= 3;
  }

  /**
   * Global Encryption / World Privacy Day: Jan 28.
   */
  public static boolean isPrivacyDayActive(@NonNull Calendar calendar) {
    return calendar.get(Calendar.MONTH) == Calendar.JANUARY
        && calendar.get(Calendar.DAY_OF_MONTH) == 28;
  }

  /**
   * Earth Day: April 22.
   */
  public static boolean isEarthDayActive(@NonNull Calendar calendar) {
    return calendar.get(Calendar.MONTH) == Calendar.APRIL
        && calendar.get(Calendar.DAY_OF_MONTH) == 22;
  }

  /**
   * Pi Day: March 14.
   */
  public static boolean isPiDayActive(@NonNull Calendar calendar) {
    return calendar.get(Calendar.MONTH) == Calendar.MARCH
        && calendar.get(Calendar.DAY_OF_MONTH) == 14;
  }

  /**
   * Star Wars Day: May 4.
   */
  public static boolean isStarWarsDayActive(@NonNull Calendar calendar) {
    return calendar.get(Calendar.MONTH) == Calendar.MAY
        && calendar.get(Calendar.DAY_OF_MONTH) == 4;
  }
}
