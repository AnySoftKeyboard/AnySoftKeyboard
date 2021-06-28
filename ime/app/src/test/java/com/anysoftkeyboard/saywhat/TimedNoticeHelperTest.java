package com.anysoftkeyboard.saywhat;

import android.os.SystemClock;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class TimedNoticeHelperTest {
    @Test
    public void testHappyPath() {
        TimedNoticeHelper helper =
                new TimedNoticeHelper(
                        ApplicationProvider.getApplicationContext(), R.string.pref_test_key, 213);
        // nothing is set now, so it should be shown
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(212, TimeUnit.MILLISECONDS);
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(2, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(213, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(2130, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
    }

    @Test
    public void testHappyPathWithProvider() {
        final AtomicLong longProvider = new AtomicLong(213);
        TimedNoticeHelper helper =
                new TimedNoticeHelper(
                        ApplicationProvider.getApplicationContext(),
                        R.string.pref_test_key,
                        timesShown -> longProvider.getAndIncrement());
        // nothing is set now, so it should be shown
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(212, TimeUnit.MILLISECONDS);
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(2, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(213 + 1, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Robolectric.getForegroundThreadScheduler().advanceBy(2130, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(213 + 2, TimeUnit.MILLISECONDS);
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(1, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(213 + 3, TimeUnit.MILLISECONDS);
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(1, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
    }

    @Test
    public void testHappyPathWithProviderWithInputTimesShown() {
        final AtomicLong longProvider = new AtomicLong(213);
        TimedNoticeHelper helper =
                new TimedNoticeHelper(
                        ApplicationProvider.getApplicationContext(),
                        R.string.pref_test_key,
                        longProvider::getAndAdd);
        // nothing is set now, so it should be shown
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(212, TimeUnit.MILLISECONDS);
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(2, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(213 + 1, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Robolectric.getForegroundThreadScheduler().advanceBy(2130, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(213 + 2, TimeUnit.MILLISECONDS);
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(1, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(213 + 5, TimeUnit.MILLISECONDS);
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(1, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
    }

    @Test
    public void testInitialData() {
        final long initialTime = SystemClock.elapsedRealtime();
        // we setting the next to be 2000
        SharedPrefsHelper.setPrefsValue(R.string.pref_test_key, Long.toString(initialTime + 2000L));
        TimedNoticeHelper helper =
                new TimedNoticeHelper(
                        ApplicationProvider.getApplicationContext(), R.string.pref_test_key, 213);

        // still not enough time passed
        Assert.assertFalse(helper.shouldShow());

        Robolectric.getForegroundThreadScheduler().advanceBy(214, TimeUnit.MILLISECONDS);
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(1586, TimeUnit.MILLISECONDS);
        Assert.assertEquals(initialTime + 1800, SystemClock.elapsedRealtime());
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(200, TimeUnit.MILLISECONDS);
        Assert.assertEquals(initialTime + 2000, SystemClock.elapsedRealtime());
        Assert.assertTrue(helper.shouldShow());

        helper.markAsShown();
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(214, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
    }

    @Test
    public void testPushesShowTimeLaterIfShownAgain() {
        TimedNoticeHelper helper =
                new TimedNoticeHelper(
                        ApplicationProvider.getApplicationContext(), R.string.pref_test_key, 213);
        // nothing is set now, so it should be shown
        Assert.assertTrue(helper.shouldShow());
        helper.markAsShown();
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(212, TimeUnit.MILLISECONDS);
        Assert.assertFalse(helper.shouldShow());
        helper.markAsShown();
        Robolectric.getForegroundThreadScheduler().advanceBy(2, TimeUnit.MILLISECONDS);
        Assert.assertFalse(helper.shouldShow());
        Robolectric.getForegroundThreadScheduler().advanceBy(214, TimeUnit.MILLISECONDS);
        Assert.assertTrue(helper.shouldShow());
    }
}
