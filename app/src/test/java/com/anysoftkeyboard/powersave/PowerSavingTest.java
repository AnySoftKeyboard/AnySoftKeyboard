package com.anysoftkeyboard.powersave;

import android.content.Intent;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class PowerSavingTest {

    @Test
    public void testValuesArray() {
        final String[] stringArray = RuntimeEnvironment.application.getResources().getStringArray(R.array.power_save_mode_values);
        Assert.assertEquals(3, stringArray.length);
        Assert.assertEquals("never", stringArray[0]);
        Assert.assertEquals("on_low_battery", stringArray[1]);
        Assert.assertEquals("always", stringArray[2]);
    }

    @Test
    public void testLifeCycle() {
        Assert.assertFalse(ShadowApplication.getInstance().hasReceiverForIntent(new Intent(Intent.ACTION_BATTERY_LOW)));
        Assert.assertFalse(ShadowApplication.getInstance().hasReceiverForIntent(new Intent(Intent.ACTION_BATTERY_OKAY)));

        final Observable<Boolean> powerSavingState = PowerSaving.observePowerSavingState(RuntimeEnvironment.application);
        final Disposable disposable = powerSavingState.subscribe(b -> {});

        Assert.assertTrue(ShadowApplication.getInstance().hasReceiverForIntent(new Intent(Intent.ACTION_BATTERY_LOW)));
        Assert.assertTrue(ShadowApplication.getInstance().hasReceiverForIntent(new Intent(Intent.ACTION_BATTERY_OKAY)));

        disposable.dispose();

        Assert.assertFalse(ShadowApplication.getInstance().hasReceiverForIntent(new Intent(Intent.ACTION_BATTERY_LOW)));
        Assert.assertFalse(ShadowApplication.getInstance().hasReceiverForIntent(new Intent(Intent.ACTION_BATTERY_OKAY)));
    }

    @Test
    public void testNeverPowerSavingMode() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode, "never");

        AtomicReference<Boolean> state = new AtomicReference<>(null);
        final Observable<Boolean> powerSavingState = PowerSaving.observePowerSavingState(RuntimeEnvironment.application);
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
        final Observable<Boolean> powerSavingState = PowerSaving.observePowerSavingState(RuntimeEnvironment.application);
        Assert.assertNull(state.get());

        final Disposable disposable = powerSavingState.subscribe(state::set);
        //starts as false
        Assert.assertEquals(Boolean.FALSE, state.get());

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
        final Observable<Boolean> powerSavingState = PowerSaving.observePowerSavingState(RuntimeEnvironment.application);
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
    
    public static void sendBatteryState(boolean lowState) {
        ShadowApplication.getInstance().sendBroadcast(new Intent(
                lowState? Intent.ACTION_BATTERY_LOW : Intent.ACTION_BATTERY_OKAY));
    }
}