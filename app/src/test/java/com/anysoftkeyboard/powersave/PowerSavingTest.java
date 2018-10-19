package com.anysoftkeyboard.powersave;

import static com.menny.android.anysoftkeyboard.R.bool.settings_default_false;
import static com.menny.android.anysoftkeyboard.R.string.settings_key_power_save_mode_sound_control;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPowerManager;

import java.util.concurrent.atomic.AtomicReference;

import androidx.test.core.app.ApplicationProvider;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class PowerSavingTest {

    @Test
    public void testValuesArray() {
        final String[] stringArray = getApplicationContext().getResources().getStringArray(R.array.power_save_mode_values);
        Assert.assertEquals(3, stringArray.length);
        Assert.assertEquals("never", stringArray[0]);
        Assert.assertEquals("on_low_battery", stringArray[1]);
        Assert.assertEquals("always", stringArray[2]);
    }

    @Test
    public void testLifeCycle() {
        Assert.assertFalse(Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).hasReceiverForIntent(new Intent(Intent.ACTION_BATTERY_LOW)));
        Assert.assertFalse(Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).hasReceiverForIntent(new Intent(Intent.ACTION_BATTERY_OKAY)));

        final Observable<Boolean> powerSavingState = PowerSaving.observePowerSavingState(getApplicationContext(), 0);
        final Disposable disposable = powerSavingState.subscribe(b -> {});

        Assert.assertTrue(Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).hasReceiverForIntent(new Intent(Intent.ACTION_BATTERY_LOW)));
        Assert.assertTrue(Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).hasReceiverForIntent(new Intent(Intent.ACTION_BATTERY_OKAY)));

        disposable.dispose();

        Assert.assertFalse(Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).hasReceiverForIntent(new Intent(Intent.ACTION_BATTERY_LOW)));
        Assert.assertFalse(Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).hasReceiverForIntent(new Intent(Intent.ACTION_BATTERY_OKAY)));
    }

    @Test
    public void testNeverPowerSavingMode() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode, "never");

        AtomicReference<Boolean> state = new AtomicReference<>(null);
        final Observable<Boolean> powerSavingState = PowerSaving.observePowerSavingState(getApplicationContext(), 0);
        Assert.assertNull(state.get());

        final Disposable disposable = powerSavingState.subscribe(state::set);
        //starts as false
        Assert.assertEquals(Boolean.FALSE, state.get());

        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());

        sendBatteryState(true);
        Assert.assertEquals(Boolean.FALSE, state.get());

        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());

        disposable.dispose();

        sendBatteryState(true);
        Assert.assertEquals(Boolean.FALSE, state.get());
        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());
    }

    @Test
    public void testAlwaysPowerSavingMode() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode, "always");

        AtomicReference<Boolean> state = new AtomicReference<>(null);
        final Observable<Boolean> powerSavingState = PowerSaving.observePowerSavingState(getApplicationContext(), 0);
        Assert.assertNull(state.get());

        final Disposable disposable = powerSavingState.subscribe(state::set);
        Assert.assertEquals(Boolean.TRUE, state.get());

        sendBatteryState(false);
        Assert.assertEquals(Boolean.TRUE, state.get());

        sendBatteryState(true);
        Assert.assertEquals(Boolean.TRUE, state.get());

        sendBatteryState(false);
        Assert.assertEquals(Boolean.TRUE, state.get());

        disposable.dispose();

        sendBatteryState(true);
        Assert.assertEquals(Boolean.TRUE, state.get());
        sendBatteryState(false);
        Assert.assertEquals(Boolean.TRUE, state.get());

    }

    @Test
    public void testWhenLowPowerSavingMode() {
        AtomicReference<Boolean> state = new AtomicReference<>(null);
        final Observable<Boolean> powerSavingState = PowerSaving.observePowerSavingState(getApplicationContext(), 0);
        Assert.assertNull(state.get());

        final Disposable disposable = powerSavingState.subscribe(state::set);
        //starts as false
        Assert.assertEquals(Boolean.FALSE, state.get());

        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());

        sendBatteryState(true);
        Assert.assertEquals(Boolean.TRUE, state.get());

        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());

        disposable.dispose();

        sendBatteryState(true);
        Assert.assertEquals(Boolean.FALSE, state.get());
        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());
    }

    @Test
    public void testControlledByEnabledPref() {
        AtomicReference<Boolean> state = new AtomicReference<>(null);
        final Observable<Boolean> powerSavingState = PowerSaving.observePowerSavingState(getApplicationContext(),
                settings_key_power_save_mode_sound_control);
        Assert.assertNull(state.get());

        final Disposable disposable = powerSavingState.subscribe(state::set);
        //starts as false
        Assert.assertEquals(Boolean.FALSE, state.get());

        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());

        sendBatteryState(true);
        Assert.assertEquals(Boolean.TRUE, state.get());

        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode_sound_control, false);
        //from this point it will always be FALSE (not low-battery)
        Assert.assertEquals(Boolean.FALSE, state.get());
        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());
        sendBatteryState(true);
        Assert.assertEquals(Boolean.FALSE, state.get());
        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());

        disposable.dispose();

        sendBatteryState(true);
        Assert.assertEquals(Boolean.FALSE, state.get());
        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode_sound_control, true);
        Assert.assertEquals(Boolean.FALSE, state.get());
        sendBatteryState(true);
        Assert.assertEquals(Boolean.FALSE, state.get());
        sendBatteryState(false);
        Assert.assertEquals(Boolean.FALSE, state.get());
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
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).sendBroadcast(new Intent(
                lowState ? Intent.ACTION_BATTERY_LOW : Intent.ACTION_BATTERY_OKAY));
    }

    public static void sendPowerSavingState(ShadowPowerManager shadowPowerManager, boolean powerSaving) {
        shadowPowerManager.setIsPowerSaveMode(powerSaving);
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).sendBroadcast(new Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED));
    }
}