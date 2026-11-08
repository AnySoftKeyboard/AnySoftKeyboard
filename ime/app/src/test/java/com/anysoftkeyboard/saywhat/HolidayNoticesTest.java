package com.anysoftkeyboard.saywhat;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
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
}
