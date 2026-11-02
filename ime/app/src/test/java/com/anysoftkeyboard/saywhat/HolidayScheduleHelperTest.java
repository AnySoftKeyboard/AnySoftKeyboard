package com.anysoftkeyboard.saywhat;

import java.util.Calendar;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

public class HolidayScheduleHelperTest {

  private Calendar createCalendar(int year, int month, int day, int hour, int minute) {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(year, month, day, hour, minute, 0);
    return cal;
  }

  @Test
  public void testThanksgivingDayOfMonth() {
    // 2023: Nov 23
    Assert.assertEquals(23, HolidayScheduleHelper.getThanksgivingDayOfMonth(2023));
    // 2024: Nov 28
    Assert.assertEquals(28, HolidayScheduleHelper.getThanksgivingDayOfMonth(2024));
    // 2025: Nov 27
    Assert.assertEquals(27, HolidayScheduleHelper.getThanksgivingDayOfMonth(2025));
    // 2026: Nov 26
    Assert.assertEquals(26, HolidayScheduleHelper.getThanksgivingDayOfMonth(2026));
  }

  @Test
  public void testThanksgivingActiveWindowAndLocale() {
    Locale us = Locale.US;
    Locale uk = Locale.UK;

    // In 2024, Thanksgiving is Nov 28. Active window is Nov 21 to Nov 28.
    Calendar before = createCalendar(2024, Calendar.NOVEMBER, 20, 10, 0);
    Calendar start = createCalendar(2024, Calendar.NOVEMBER, 21, 10, 0);
    Calendar middle = createCalendar(2024, Calendar.NOVEMBER, 25, 10, 0);
    Calendar dayOf = createCalendar(2024, Calendar.NOVEMBER, 28, 23, 59);
    Calendar after = createCalendar(2024, Calendar.NOVEMBER, 29, 0, 1);
    Calendar wrongMonth = createCalendar(2024, Calendar.OCTOBER, 25, 10, 0);

    Assert.assertFalse(HolidayScheduleHelper.isThanksgivingActive(before, us));
    Assert.assertTrue(HolidayScheduleHelper.isThanksgivingActive(start, us));
    Assert.assertTrue(HolidayScheduleHelper.isThanksgivingActive(middle, us));
    Assert.assertTrue(HolidayScheduleHelper.isThanksgivingActive(dayOf, us));
    Assert.assertFalse(HolidayScheduleHelper.isThanksgivingActive(after, us));
    Assert.assertFalse(HolidayScheduleHelper.isThanksgivingActive(wrongMonth, us));

    // Locale check
    Assert.assertFalse(HolidayScheduleHelper.isThanksgivingActive(dayOf, uk));
    Assert.assertFalse(HolidayScheduleHelper.isThanksgivingActive(dayOf, null));
  }

  @Test
  public void testHalloweenActiveWindowAndLocales() {
    String[] observing = {"US", "ca", "Gb", "IE", "AU", "nz", "MX"};
    for (String country : observing) {
      Assert.assertTrue(HolidayScheduleHelper.isHalloweenObservingLocale(country));
    }
    Assert.assertFalse(HolidayScheduleHelper.isHalloweenObservingLocale("FR"));
    Assert.assertFalse(HolidayScheduleHelper.isHalloweenObservingLocale("DE"));
    Assert.assertFalse(HolidayScheduleHelper.isHalloweenObservingLocale(null));

    Locale us = Locale.US;
    Calendar oct28 = createCalendar(2024, Calendar.OCTOBER, 28, 12, 0);
    Calendar oct29 = createCalendar(2024, Calendar.OCTOBER, 29, 0, 0);
    Calendar oct30 = createCalendar(2024, Calendar.OCTOBER, 30, 12, 0);
    Calendar oct31 = createCalendar(2024, Calendar.OCTOBER, 31, 23, 59);
    Calendar nov1 = createCalendar(2024, Calendar.NOVEMBER, 1, 0, 0);

    Assert.assertFalse(HolidayScheduleHelper.isHalloweenActive(oct28, us));
    Assert.assertTrue(HolidayScheduleHelper.isHalloweenActive(oct29, us));
    Assert.assertTrue(HolidayScheduleHelper.isHalloweenActive(oct30, us));
    Assert.assertTrue(HolidayScheduleHelper.isHalloweenActive(oct31, us));
    Assert.assertFalse(HolidayScheduleHelper.isHalloweenActive(nov1, us));

    Assert.assertFalse(HolidayScheduleHelper.isHalloweenActive(oct31, Locale.GERMANY));
    Assert.assertFalse(HolidayScheduleHelper.isHalloweenActive(oct31, null));
  }

  @Test
  public void testNewYearsEve() {
    Calendar dec30 = createCalendar(2024, Calendar.DECEMBER, 30, 22, 0);
    Calendar dec31Early = createCalendar(2024, Calendar.DECEMBER, 31, 20, 59);
    Calendar dec31Active1 = createCalendar(2024, Calendar.DECEMBER, 31, 21, 0);
    Calendar dec31Active2 = createCalendar(2024, Calendar.DECEMBER, 31, 23, 59);
    Calendar jan1 = createCalendar(2025, Calendar.JANUARY, 1, 0, 1);

    Assert.assertFalse(HolidayScheduleHelper.isNewYearsEveActive(dec30));
    Assert.assertFalse(HolidayScheduleHelper.isNewYearsEveActive(dec31Early));
    Assert.assertTrue(HolidayScheduleHelper.isNewYearsEveActive(dec31Active1));
    Assert.assertTrue(HolidayScheduleHelper.isNewYearsEveActive(dec31Active2));
    Assert.assertFalse(HolidayScheduleHelper.isNewYearsEveActive(jan1));
  }

  @Test
  public void testNewYearsDay() {
    Calendar dec31 = createCalendar(2024, Calendar.DECEMBER, 31, 23, 59);
    Calendar jan1 = createCalendar(2025, Calendar.JANUARY, 1, 10, 0);
    Calendar jan2 = createCalendar(2025, Calendar.JANUARY, 2, 10, 0);
    Calendar jan3 = createCalendar(2025, Calendar.JANUARY, 3, 23, 59);
    Calendar jan4 = createCalendar(2025, Calendar.JANUARY, 4, 0, 0);

    // Keyboard only active Jan 1
    Assert.assertFalse(HolidayScheduleHelper.isNewYearsDayKeyboardActive(dec31));
    Assert.assertTrue(HolidayScheduleHelper.isNewYearsDayKeyboardActive(jan1));
    Assert.assertFalse(HolidayScheduleHelper.isNewYearsDayKeyboardActive(jan2));

    // UI active Jan 1 - Jan 3
    Assert.assertFalse(HolidayScheduleHelper.isNewYearsDayUiActive(dec31));
    Assert.assertTrue(HolidayScheduleHelper.isNewYearsDayUiActive(jan1));
    Assert.assertTrue(HolidayScheduleHelper.isNewYearsDayUiActive(jan2));
    Assert.assertTrue(HolidayScheduleHelper.isNewYearsDayUiActive(jan3));
    Assert.assertFalse(HolidayScheduleHelper.isNewYearsDayUiActive(jan4));
  }

  @Test
  public void testSingleDayObservances() {
    // Privacy Day Jan 28
    Assert.assertFalse(
        HolidayScheduleHelper.isPrivacyDayActive(createCalendar(2025, Calendar.JANUARY, 27, 0, 0)));
    Assert.assertTrue(
        HolidayScheduleHelper.isPrivacyDayActive(
            createCalendar(2025, Calendar.JANUARY, 28, 12, 0)));
    Assert.assertFalse(
        HolidayScheduleHelper.isPrivacyDayActive(createCalendar(2025, Calendar.JANUARY, 29, 0, 0)));

    // Earth Day April 22
    Assert.assertFalse(
        HolidayScheduleHelper.isEarthDayActive(createCalendar(2025, Calendar.APRIL, 21, 0, 0)));
    Assert.assertTrue(
        HolidayScheduleHelper.isEarthDayActive(createCalendar(2025, Calendar.APRIL, 22, 12, 0)));
    Assert.assertFalse(
        HolidayScheduleHelper.isEarthDayActive(createCalendar(2025, Calendar.APRIL, 23, 0, 0)));

    // Pi Day March 14
    Assert.assertFalse(
        HolidayScheduleHelper.isPiDayActive(createCalendar(2025, Calendar.MARCH, 13, 0, 0)));
    Assert.assertTrue(
        HolidayScheduleHelper.isPiDayActive(createCalendar(2025, Calendar.MARCH, 14, 12, 0)));
    Assert.assertFalse(
        HolidayScheduleHelper.isPiDayActive(createCalendar(2025, Calendar.MARCH, 15, 0, 0)));

    // Star Wars Day May 4
    Assert.assertFalse(
        HolidayScheduleHelper.isStarWarsDayActive(createCalendar(2025, Calendar.MAY, 3, 0, 0)));
    Assert.assertTrue(
        HolidayScheduleHelper.isStarWarsDayActive(createCalendar(2025, Calendar.MAY, 4, 12, 0)));
    Assert.assertFalse(
        HolidayScheduleHelper.isStarWarsDayActive(createCalendar(2025, Calendar.MAY, 5, 0, 0)));
  }
}
