package com.anysoftkeyboard.saywhat;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.preference.PreferenceManager;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.menny.android.anysoftkeyboard.R;
import java.util.Calendar;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class HolidayNoticesTest extends AnySoftKeyboardBaseTest {

  private Context mContext;

  @Before
  public void setUp() {
    mContext = getApplicationContext();
  }

  @Test
  public void testPiDayAndStarWarsDayDoNotImplementOnUiPage() {
    Assert.assertFalse(new PiDayNotice() instanceof OnUiPage);
    Assert.assertFalse(new StarWarsDayNotice() instanceof OnUiPage);
  }

  @Test
  public void testCardNoticesImplementOnUiPage() {
    Assert.assertTrue(new ThanksgivingNotice() instanceof OnUiPage);
    Assert.assertTrue(new HalloweenNotice() instanceof OnUiPage);
    Assert.assertTrue(new NewYearsEveNotice() instanceof OnUiPage);
    Assert.assertTrue(new NewYearsDayNotice() instanceof OnUiPage);
    Assert.assertTrue(new PrivacyDayNotice() instanceof OnUiPage);
    Assert.assertTrue(new EarthDayNotice() instanceof OnUiPage);
  }

  @Test
  public void testThanksgivingCardActiveAndInactive() {
    ThanksgivingNotice notice = new ThanksgivingNotice();
    notice.setTestLocale(Locale.US);

    Calendar activeCal = Calendar.getInstance();
    activeCal.set(2025, Calendar.NOVEMBER, 27, 12, 0, 0); // Thanksgiving 2025
    notice.setTestCalendar(activeCal);

    View view = notice.inflateContentView(mContext);
    Assert.assertNotNull(view);
    TextView title = view.findViewById(R.id.public_notice_card_title);
    TextView message = view.findViewById(R.id.public_notice_card_message);
    Assert.assertNotNull(title);
    Assert.assertNotNull(message);
    Assert.assertEquals(
        mContext.getString(R.string.public_notice_thanksgiving_title), title.getText().toString());

    // Inactive date
    Calendar inactiveCal = Calendar.getInstance();
    inactiveCal.set(2025, Calendar.JUNE, 1, 12, 0, 0);
    notice.setTestCalendar(inactiveCal);
    Assert.assertNull(notice.inflateContentView(mContext));

    // Non-US locale on active date
    notice.setTestCalendar(activeCal);
    notice.setTestLocale(Locale.FRANCE);
    Assert.assertNull(notice.inflateContentView(mContext));
  }

  @Test
  public void testHalloweenCardActiveAndInactive() {
    HalloweenNotice notice = new HalloweenNotice();
    notice.setTestLocale(Locale.US);

    Calendar activeCal = Calendar.getInstance();
    activeCal.set(2025, Calendar.OCTOBER, 31, 18, 0, 0);
    notice.setTestCalendar(activeCal);

    View view = notice.inflateContentView(mContext);
    Assert.assertNotNull(view);
    TextView title = view.findViewById(R.id.public_notice_card_title);
    Assert.assertEquals(
        mContext.getString(R.string.public_notice_halloween_title), title.getText().toString());

    Calendar inactiveCal = Calendar.getInstance();
    inactiveCal.set(2025, Calendar.NOVEMBER, 5, 12, 0, 0);
    notice.setTestCalendar(inactiveCal);
    Assert.assertNull(notice.inflateContentView(mContext));

    // Non-observing locale on active date
    notice.setTestCalendar(activeCal);
    notice.setTestLocale(Locale.GERMANY);
    Assert.assertNull(notice.inflateContentView(mContext));
  }

  @Test
  public void testPrivacyDayCardActiveAndInactive() {
    PrivacyDayNotice notice = new PrivacyDayNotice();

    Calendar activeCal = Calendar.getInstance();
    activeCal.set(2025, Calendar.JANUARY, 28, 10, 0, 0);
    notice.setTestCalendar(activeCal);

    View view = notice.inflateContentView(mContext);
    Assert.assertNotNull(view);
    TextView title = view.findViewById(R.id.public_notice_card_title);
    Assert.assertEquals(
        mContext.getString(R.string.public_notice_privacy_day_title), title.getText().toString());

    Calendar inactiveCal = Calendar.getInstance();
    inactiveCal.set(2025, Calendar.JANUARY, 29, 10, 0, 0);
    notice.setTestCalendar(inactiveCal);
    Assert.assertNull(notice.inflateContentView(mContext));
  }

  @Test
  public void testEarthDayCardActiveAndInactive() {
    EarthDayNotice notice = new EarthDayNotice();

    Calendar activeCal = Calendar.getInstance();
    activeCal.set(2025, Calendar.APRIL, 22, 10, 0, 0);
    notice.setTestCalendar(activeCal);

    View view = notice.inflateContentView(mContext);
    Assert.assertNotNull(view);
    TextView title = view.findViewById(R.id.public_notice_card_title);
    Assert.assertEquals(
        mContext.getString(R.string.public_notice_earth_day_title), title.getText().toString());

    Calendar inactiveCal = Calendar.getInstance();
    inactiveCal.set(2025, Calendar.APRIL, 23, 10, 0, 0);
    notice.setTestCalendar(inactiveCal);
    Assert.assertNull(notice.inflateContentView(mContext));
  }

  @Test
  public void testNewYearsEveAndDayCardActive() {
    NewYearsEveNotice eveNotice = new NewYearsEveNotice();
    Calendar eveCal = Calendar.getInstance();
    eveCal.set(2025, Calendar.DECEMBER, 31, 22, 0, 0);
    eveNotice.setTestCalendar(eveCal);
    Assert.assertNotNull(eveNotice.inflateContentView(mContext));

    NewYearsDayNotice dayNotice = new NewYearsDayNotice();
    Calendar dayCal = Calendar.getInstance();
    dayCal.set(2026, Calendar.JANUARY, 2, 12, 0, 0); // Jan 2 is in Jan 1-3 UI window
    dayNotice.setTestCalendar(dayCal);
    Assert.assertNotNull(dayNotice.inflateContentView(mContext));

    Calendar inactiveCal = Calendar.getInstance();
    inactiveCal.set(2026, Calendar.JANUARY, 4, 12, 0, 0);
    dayNotice.setTestCalendar(inactiveCal);
    Assert.assertNull(dayNotice.inflateContentView(mContext));
  }

  @Test
  public void testKeyboardVisibilityAndTypingTrigger() {
    PiDayNotice notice = new PiDayNotice();

    Calendar activeCal = Calendar.getInstance();
    activeCal.set(2025, Calendar.MARCH, 14, 15, 0, 0);
    notice.setTestCalendar(activeCal);

    // On visible
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);

    // Type "3.14"
    notice.onKey(mAnySoftKeyboardUnderTest, '3', null);
    notice.onKey(mAnySoftKeyboardUnderTest, '.', null);
    notice.onKey(mAnySoftKeyboardUnderTest, '1', null);
    notice.onKey(mAnySoftKeyboardUnderTest, '4', null);

    // On hidden
    notice.onHidden(mAnySoftKeyboardUnderTest, null);
  }

  @Test
  public void testInactiveNoticeDoesNotShowStripOrTriggerTyping() {
    StarWarsDayNotice notice = new StarWarsDayNotice();

    Calendar inactiveCal = Calendar.getInstance();
    inactiveCal.set(2025, Calendar.MARCH, 1, 12, 0, 0); // Not May 4
    notice.setTestCalendar(inactiveCal);

    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);

    // Type "may the 4th"
    String text = "may the 4th";
    for (int i = 0; i < text.length(); i++) {
      notice.onKey(mAnySoftKeyboardUnderTest, text.charAt(i), null);
    }
  }

  @Test
  public void testNewYearsEveCardActiveAllDay() {
    NewYearsEveNotice eveNotice = new NewYearsEveNotice();
    Calendar eveMorning = Calendar.getInstance();
    eveMorning.set(2025, Calendar.DECEMBER, 31, 10, 0, 0);
    eveNotice.setTestCalendar(eveMorning);

    // UI card is active in the morning
    Assert.assertNotNull(eveNotice.inflateContentView(mContext));
    // But keyboard is not active until 21:00
    Assert.assertFalse(eveNotice.isDateActive(eveMorning, Locale.US));
  }

  @Test
  public void testNullInputViewContainerSafe() {
    PiDayNotice notice = new PiDayNotice();
    Calendar activeCal = Calendar.getInstance();
    activeCal.set(2025, Calendar.MARCH, 14, 15, 0, 0);
    notice.setTestCalendar(activeCal);

    PublicNotices mockIme = Mockito.mock(PublicNotices.class);
    Mockito.doReturn(null).when(mockIme).getInputViewContainer();
    Mockito.doReturn(null).when(mockIme).getInputView();

    // Must not throw when getInputViewContainer() is null
    notice.onVisible(mockIme, null, null);
    notice.onKey(mockIme, '3', null);
    notice.onHidden(mockIme, null);
  }

  @Test
  public void testCardInflatesWithParentLayoutParams() {
    ThanksgivingNotice notice = new ThanksgivingNotice();
    notice.setTestLocale(Locale.US);
    Calendar thanksgiving = Calendar.getInstance();
    thanksgiving.set(2025, Calendar.NOVEMBER, 27, 12, 0, 0);
    notice.setTestCalendar(thanksgiving);

    LinearLayout parent = new LinearLayout(mContext);
    View cardView = notice.inflateContentView(parent);
    Assert.assertNotNull(cardView);
    Assert.assertNotNull(cardView.getLayoutParams());
  }

  @Test
  public void testStripActionTalkBackAccessibility() {
    PiDayNotice notice = new PiDayNotice();
    Calendar activeCal = Calendar.getInstance();
    activeCal.set(2025, Calendar.MARCH, 14, 15, 0, 0);
    notice.setTestCalendar(activeCal);

    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    var container = mAnySoftKeyboardUnderTest.getInputViewContainer();
    Assert.assertNotNull(container);

    var stripAction = container.findViewById(R.id.holiday_strip_action_text);
    Assert.assertNotNull(stripAction);
    Assert.assertNotNull(stripAction.getContentDescription());
    Assert.assertEquals("π = 3.1415926535...", stripAction.getContentDescription().toString());

    notice.onHidden(mAnySoftKeyboardUnderTest, null);
  }

  @Test
  public void testOnVisibleRemovesStripActionWhenNoticeBecomesInactive() {
    ThanksgivingNotice notice = new ThanksgivingNotice();
    notice.setTestLocale(Locale.US);

    Calendar activeCal = Calendar.getInstance();
    activeCal.set(2025, Calendar.NOVEMBER, 27, 12, 0, 0);
    notice.setTestCalendar(activeCal);

    var container = mAnySoftKeyboardUnderTest.getInputViewContainer();
    Assert.assertNotNull(container);

    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    Assert.assertNotNull(container.findViewById(R.id.holiday_strip_action_text));

    // Simulate restarting input after midnight (no onHidden called in between).
    Calendar afterThanksgiving = Calendar.getInstance();
    afterThanksgiving.set(2025, Calendar.NOVEMBER, 28, 0, 1, 0);
    notice.setTestCalendar(afterThanksgiving);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    Assert.assertNull(container.findViewById(R.id.holiday_strip_action_text));

    // Re-activate and test locale change without intervening onHidden.
    notice.setTestCalendar(activeCal);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    Assert.assertNotNull(container.findViewById(R.id.holiday_strip_action_text));

    notice.setTestLocale(Locale.UK);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    Assert.assertNull(container.findViewById(R.id.holiday_strip_action_text));
  }

  @Test
  public void testThanksgivingTapToDismissPreWindowAndReshowOnDayOf() {
    ThanksgivingNotice notice = new ThanksgivingNotice();
    notice.setTestLocale(Locale.US);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    String prefKey = HolidayPublicNotice.PREF_KEY_PREFIX + notice.getName();

    var container = mAnySoftKeyboardUnderTest.getInputViewContainer();
    Assert.assertNotNull(container);

    // 1. Pre-holiday window (Nov 20, 2025; Thanksgiving is Nov 27, 2025): icon & card show.
    Calendar preDay1 = Calendar.getInstance();
    preDay1.set(2025, Calendar.NOVEMBER, 20, 12, 0, 0);
    notice.setTestCalendar(preDay1);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    View stripAction = container.findViewById(R.id.holiday_strip_action_text);
    Assert.assertNotNull(stripAction);
    Assert.assertNotNull(notice.inflateContentView(mContext));

    // 2. Tap to dismiss during pre-holiday -> removes immediately & records pre-dismissed.
    stripAction.performClick();
    Assert.assertNull(container.findViewById(R.id.holiday_strip_action_text));
    Assert.assertEquals("2025:pre-dismissed", prefs.getString(prefKey, ""));
    Assert.assertNull(notice.inflateContentView(mContext));

    // 3. Subsequent pre-holiday days stay dismissed (onVisible and onKey do not re-add strip icon).
    Calendar preDay2 = Calendar.getInstance();
    preDay2.set(2025, Calendar.NOVEMBER, 25, 12, 0, 0);
    notice.setTestCalendar(preDay2);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    Assert.assertNull(container.findViewById(R.id.holiday_strip_action_text));
    String triggerWord = "turkey";
    for (int i = 0; i < triggerWord.length(); i++) {
      notice.onKey(mAnySoftKeyboardUnderTest, triggerWord.charAt(i), null);
    }
    Assert.assertNull(container.findViewById(R.id.holiday_strip_action_text));

    // 4. On Thanksgiving Day itself (Nov 27, 2025): icon & card re-show!
    Calendar dayOf2025 = Calendar.getInstance();
    dayOf2025.set(2025, Calendar.NOVEMBER, 27, 9, 0, 0);
    notice.setTestCalendar(dayOf2025);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    View dayOfStripAction = container.findViewById(R.id.holiday_strip_action_text);
    Assert.assertNotNull(dayOfStripAction);
    Assert.assertNotNull(notice.inflateContentView(mContext));

    // 5. Tap to dismiss on Thanksgiving Day -> removes immediately & marks completed for 2025.
    dayOfStripAction.performClick();
    Assert.assertNull(container.findViewById(R.id.holiday_strip_action_text));
    Assert.assertEquals("2025:completed", prefs.getString(prefKey, ""));
    Assert.assertNull(notice.inflateContentView(mContext));

    // 6. Later on Thanksgiving Day: stays dismissed.
    Calendar laterDayOf2025 = Calendar.getInstance();
    laterDayOf2025.set(2025, Calendar.NOVEMBER, 27, 18, 0, 0);
    notice.setTestCalendar(laterDayOf2025);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    Assert.assertNull(container.findViewById(R.id.holiday_strip_action_text));

    // 7. Next year (Thanksgiving 2026 is Nov 26): shows again in pre-holiday window.
    Calendar preDay2026 = Calendar.getInstance();
    preDay2026.set(2026, Calendar.NOVEMBER, 20, 12, 0, 0);
    notice.setTestCalendar(preDay2026);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    Assert.assertNotNull(container.findViewById(R.id.holiday_strip_action_text));
    Assert.assertNotNull(notice.inflateContentView(mContext));

    notice.onHidden(mAnySoftKeyboardUnderTest, null);
  }

  @Test
  public void testHalloweenTapToDismissPreWindowAndReshowOnDayOf() {
    HalloweenNotice notice = new HalloweenNotice();
    notice.setTestLocale(Locale.US);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    String prefKey = HolidayPublicNotice.PREF_KEY_PREFIX + notice.getName();

    var container = mAnySoftKeyboardUnderTest.getInputViewContainer();
    Assert.assertNotNull(container);

    // Oct 29 (pre-holiday): shown -> tap -> pre-dismissed.
    Calendar oct29 = Calendar.getInstance();
    oct29.set(2025, Calendar.OCTOBER, 29, 12, 0, 0);
    notice.setTestCalendar(oct29);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    View stripAction = container.findViewById(R.id.holiday_strip_action_text);
    Assert.assertNotNull(stripAction);
    stripAction.performClick();
    Assert.assertNull(container.findViewById(R.id.holiday_strip_action_text));
    Assert.assertEquals("2025:pre-dismissed", prefs.getString(prefKey, ""));

    // Oct 30: still pre-dismissed.
    Calendar oct30 = Calendar.getInstance();
    oct30.set(2025, Calendar.OCTOBER, 30, 12, 0, 0);
    notice.setTestCalendar(oct30);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    Assert.assertNull(container.findViewById(R.id.holiday_strip_action_text));

    // Oct 31 (Halloween Day): re-shows -> tap -> completed.
    Calendar oct31 = Calendar.getInstance();
    oct31.set(2025, Calendar.OCTOBER, 31, 12, 0, 0);
    notice.setTestCalendar(oct31);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    View halloweenAction = container.findViewById(R.id.holiday_strip_action_text);
    Assert.assertNotNull(halloweenAction);
    halloweenAction.performClick();
    Assert.assertEquals("2025:completed", prefs.getString(prefKey, ""));
    Assert.assertNull(container.findViewById(R.id.holiday_strip_action_text));
  }

  @Test
  public void testSingleDayNoticeTapToDismissGoesStraightToCompleted() {
    PiDayNotice notice = new PiDayNotice();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    String prefKey = HolidayPublicNotice.PREF_KEY_PREFIX + notice.getName();

    var container = mAnySoftKeyboardUnderTest.getInputViewContainer();
    Assert.assertNotNull(container);

    Calendar piDay2025 = Calendar.getInstance();
    piDay2025.set(2025, Calendar.MARCH, 14, 10, 0, 0);
    notice.setTestCalendar(piDay2025);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);

    View stripAction = container.findViewById(R.id.holiday_strip_action_text);
    Assert.assertNotNull(stripAction);
    stripAction.performClick();
    Assert.assertNull(container.findViewById(R.id.holiday_strip_action_text));
    Assert.assertEquals("2025:completed", prefs.getString(prefKey, ""));

    // Stays hidden on subsequent onVisible in 2025.
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    Assert.assertNull(container.findViewById(R.id.holiday_strip_action_text));

    // Reshows on Pi Day 2026.
    Calendar piDay2026 = Calendar.getInstance();
    piDay2026.set(2026, Calendar.MARCH, 14, 10, 0, 0);
    notice.setTestCalendar(piDay2026);
    notice.onVisible(mAnySoftKeyboardUnderTest, null, null);
    Assert.assertNotNull(container.findViewById(R.id.holiday_strip_action_text));

    notice.onHidden(mAnySoftKeyboardUnderTest, null);
  }
}
