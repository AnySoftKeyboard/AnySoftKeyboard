package com.anysoftkeyboard.saywhat;

import java.util.Calendar;

class PeriodsTimeProvider implements TimedNoticeHelper.NextTimeProvider {
  // 1 day
  static final long ONE_DAY = 24 * 60 * 60 * 1000;

  interface CalendarFactory {
    Calendar getInstance();
  }

  private final CalendarFactory mCalendarProvider;
  private final int mFirstNoticeStart;
  private final int mFirstNoticeEnd;
  private final int mSecondNoticeStart;
  private final int mSecondNoticeEnd;

  PeriodsTimeProvider(
      CalendarFactory nowCalendarProvider,
      int firstNoticeStart,
      int firstNoticeEnd,
      int secondNoticeStart,
      int secondNoticeEnd) {
    mCalendarProvider = nowCalendarProvider;
    mFirstNoticeStart = firstNoticeStart;
    mFirstNoticeEnd = firstNoticeEnd;
    mSecondNoticeStart = secondNoticeStart;
    mSecondNoticeEnd = secondNoticeEnd;
  }

  PeriodsTimeProvider(
      int firstNoticeStart, int firstNoticeEnd, int secondNoticeStart, int secondNoticeEnd) {
    this(
        Calendar::getInstance,
        firstNoticeStart,
        firstNoticeEnd,
        secondNoticeStart,
        secondNoticeEnd);
  }

  @Override
  public long getNextTimeOffset(int timesShown) {
    final Calendar calendar = mCalendarProvider.getInstance();
    final int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
    if (dayOfYear < mFirstNoticeStart) {
      // should start showing at May start
      return ONE_DAY * (mFirstNoticeStart - dayOfYear);
    } else if (dayOfYear < mFirstNoticeEnd) {
      // inside notice period. Notify every day
      return ONE_DAY;
    } else if (dayOfYear < mSecondNoticeStart) {
      // should start showing at November start
      return ONE_DAY * (mSecondNoticeStart - dayOfYear);
    } else if (dayOfYear < mSecondNoticeEnd) {
      // inside notice period. Notify every day
      return ONE_DAY;
    } else {
      // till next May
      return ONE_DAY * (365 + mFirstNoticeStart - dayOfYear);
    }
  }
}
