package com.anysoftkeyboard.ime;

import static com.anysoftkeyboard.android.NightModeTest.configurationForNightMode;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.ComponentName;
import android.content.res.Configuration;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlyDataCreator;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import androidx.test.core.app.ApplicationProvider;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardNightModeTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testIconShownWhenTriggered() throws Exception {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode, "follow_system");
        AnyApplication application = getApplicationContext();
        //initial watermark
        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_night_mode);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));

        ViewTestUtils.assertCurrentWatermarkHasDrawable(mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_night_mode);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_NO));

        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_night_mode);
    }

    @Test
    public void testIconShownWhenAlwaysOn() throws Exception {
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode, "always");
        ViewTestUtils.assertCurrentWatermarkHasDrawable(mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_night_mode);
    }

    @Test
    public void testIconShownWhenNever() throws Exception {
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode, "never");
        AnyApplication application = getApplicationContext();
        ViewTestUtils.assertZeroWatermarkInteractions(mAnySoftKeyboardUnderTest.getInputView());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));

        ViewTestUtils.assertZeroWatermarkInteractions(mAnySoftKeyboardUnderTest.getInputView());
    }

    @Test
    public void testSetNightModeOverlay() {
        AnyApplication application = getApplicationContext();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode, "follow_system");
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode_theme_control, true);

        final OverlyDataCreator originalOverlayDataCreator = mAnySoftKeyboardUnderTest.getOriginalOverlayDataCreator();

        Assert.assertTrue(originalOverlayDataCreator instanceof AnySoftKeyboardPowerSaving.ToggleOverlayCreator);

        final OverlayData normal = originalOverlayDataCreator.createOverlayData(new ComponentName(ApplicationProvider.getApplicationContext(), MainSettingsActivity.class));
        Assert.assertNotEquals(0xFF222222, normal.getPrimaryColor());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));

        final OverlayData nightModeOverlay = originalOverlayDataCreator.createOverlayData(new ComponentName(ApplicationProvider.getApplicationContext(), MainSettingsActivity.class));
        Assert.assertTrue(nightModeOverlay.isValid());
        Assert.assertEquals(0xFF222222, nightModeOverlay.getPrimaryColor());
    }
}