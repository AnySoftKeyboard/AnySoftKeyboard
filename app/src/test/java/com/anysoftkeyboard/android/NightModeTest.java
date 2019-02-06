package com.anysoftkeyboard.android;

import static com.menny.android.anysoftkeyboard.R.bool.settings_default_false;
import static com.menny.android.anysoftkeyboard.R.string.settings_key_power_save_mode_sound_control;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.PowerManager;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPowerManager;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import androidx.test.core.app.ApplicationProvider;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class NightModeTest {

    @Test
    public void testValuesArray() {
        final String[] stringArray = getApplicationContext().getResources().getStringArray(R.array.night_mode_values);
        Assert.assertEquals(3, stringArray.length);
        Assert.assertEquals("never", stringArray[0]);
        Assert.assertEquals("follow_system", stringArray[1]);
        Assert.assertEquals("always", stringArray[2]);

        Assert.assertEquals(getApplicationContext().getString(R.string.settings_default_night_mode_value), "follow_system");
    }

    public static Configuration configurationForNightMode(int nightMode) {
        final Configuration configuration = getApplicationContext().getResources().getConfiguration();
        configuration.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK;
        configuration.uiMode |= nightMode;

        return configuration;
    }

    @Test
    public void testApplicationObservable() {
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        AnyApplication application = getApplicationContext();
        final Disposable subscribe = application.getNightModeObservable().subscribe(atomicBoolean::set, throwable -> { throw new RuntimeException(throwable); });
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));
        Assert.assertTrue(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_NO));
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_UNDEFINED));
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));
        Assert.assertTrue(atomicBoolean.get());

        subscribe.dispose();

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_NO));
        //stays the same
        Assert.assertTrue(atomicBoolean.get());
    }

    @Test
    public void testNeverNightMode() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode, "never");

        AtomicBoolean atomicBoolean = new AtomicBoolean();
        AnyApplication application = getApplicationContext();
        final Disposable subscribe = NightMode.observeNightModeState(application, 0, R.bool.settings_default_true).subscribe(atomicBoolean::set, throwable -> {
            throw new RuntimeException(throwable);
        });
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_NO));
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_UNDEFINED));
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));
        Assert.assertFalse(atomicBoolean.get());

        subscribe.dispose();
    }

    @Test
    public void testAlwaysNightMode() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode, "always");

        AtomicBoolean atomicBoolean = new AtomicBoolean();
        AnyApplication application = getApplicationContext();
        final Disposable subscribe = NightMode.observeNightModeState(application, 0, R.bool.settings_default_true).subscribe(atomicBoolean::set, throwable -> {
            throw new RuntimeException(throwable);
        });
        Assert.assertTrue(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));
        Assert.assertTrue(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_NO));
        Assert.assertTrue(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_UNDEFINED));
        Assert.assertTrue(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));
        Assert.assertTrue(atomicBoolean.get());

        subscribe.dispose();
    }

    @Test
    public void testFollowSystemNightMode() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode, "follow_system");

        AtomicBoolean atomicBoolean = new AtomicBoolean();
        AnyApplication application = getApplicationContext();
        final Disposable subscribe = NightMode.observeNightModeState(application, 0, R.bool.settings_default_true).subscribe(atomicBoolean::set, throwable -> {
            throw new RuntimeException(throwable);
        });
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));
        Assert.assertTrue(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_NO));
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_UNDEFINED));
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));
        Assert.assertTrue(atomicBoolean.get());

        subscribe.dispose();

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_NO));
        //stays the same
        Assert.assertTrue(atomicBoolean.get());
    }

    @Test
    public void testControlledByEnabledPref() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode, "follow_system");

        AtomicBoolean atomicBoolean = new AtomicBoolean();
        AnyApplication application = getApplicationContext();
        final Disposable subscribe = NightMode.observeNightModeState(application, R.string.settings_key_night_mode_app_theme_control, R.bool.settings_default_true).subscribe(atomicBoolean::set,
                throwable -> {
                    throw new RuntimeException(throwable);
                });
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));
        Assert.assertTrue(atomicBoolean.get());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode_app_theme_control, false);
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_NO));
        Assert.assertFalse(atomicBoolean.get());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_night_mode_app_theme_control, true);
        Assert.assertFalse(atomicBoolean.get());

        application.onConfigurationChanged(configurationForNightMode(Configuration.UI_MODE_NIGHT_YES));
        Assert.assertTrue(atomicBoolean.get());

        subscribe.dispose();
    }

    @Test
    public void testControlledByEnabledPrefDefaultFalse() {
        AtomicReference<Boolean> state = new AtomicReference<>(null);
        final Observable<Boolean> powerSavingState = PowerSaving.observePowerSavingState(getApplicationContext(),
                settings_key_power_save_mode_sound_control,
                settings_default_false);
        Assert.assertNull(state.get());

        final Disposable disposable = powerSavingState.subscribe(state::set);
        //starts as false
        Assert.assertEquals(Boolean.FALSE, state.get());

        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());

        sendBatteryState(true);
        Assert.assertEquals(Boolean.FALSE, state.get());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode_sound_control, true);

        Assert.assertEquals(Boolean.TRUE, state.get());
        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());
        sendBatteryState(true);
        Assert.assertEquals(Boolean.TRUE, state.get());
        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());

        disposable.dispose();
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    public void testWhenLowPowerSavingModeWithDevicePowerSavingState() {
        Context context = Mockito.spy(getApplicationContext());
        final PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Service.POWER_SERVICE);
        Mockito.doReturn(powerManager).when(context).getSystemService(Service.POWER_SERVICE);
        ShadowPowerManager shadowPowerManager = Shadows.shadowOf(powerManager);

        AtomicReference<Boolean> state = new AtomicReference<>(null);
        final Observable<Boolean> powerSavingState = PowerSaving.observePowerSavingState(context, 0);
        Assert.assertNull(state.get());

        final Disposable disposable = powerSavingState.subscribe(state::set);
        //starts as false
        Assert.assertEquals(Boolean.FALSE, state.get());

        sendPowerSavingState(shadowPowerManager, false);
        Assert.assertEquals(Boolean.FALSE, state.get());

        sendPowerSavingState(shadowPowerManager, true);
        Assert.assertEquals(Boolean.TRUE, state.get());

        sendPowerSavingState(shadowPowerManager, false);
        Assert.assertEquals(Boolean.FALSE, state.get());

        disposable.dispose();

        sendPowerSavingState(shadowPowerManager, true);
        Assert.assertEquals(Boolean.FALSE, state.get());
        sendPowerSavingState(shadowPowerManager, false);
        Assert.assertEquals(Boolean.FALSE, state.get());
    }

    public static void sendBatteryState(boolean lowState) {
        ApplicationProvider.getApplicationContext().sendBroadcast(new Intent(
                lowState ? Intent.ACTION_BATTERY_LOW : Intent.ACTION_BATTERY_OKAY));
    }

    public static void sendPowerSavingState(ShadowPowerManager shadowPowerManager, boolean powerSaving) {
        shadowPowerManager.setIsPowerSaveMode(powerSaving);
        ApplicationProvider.getApplicationContext().sendBroadcast(new Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED));
    }
}