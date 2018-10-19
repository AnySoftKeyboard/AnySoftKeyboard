package com.menny.android.anysoftkeyboard;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnyApplicationTest {

    @Test
    public void testSettingsAppIcon() {
        final PackageManager packageManager = getApplicationContext().getPackageManager();
        final ComponentName componentName = new ComponentName(getApplicationContext(), LauncherSettingsActivity.class);

        Assert.assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED, packageManager.getComponentEnabledSetting(componentName));

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_show_settings_app, false);

        Assert.assertEquals(PackageManager.COMPONENT_ENABLED_STATE_DISABLED, packageManager.getComponentEnabledSetting(componentName));

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_show_settings_app, true);

        Assert.assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED, packageManager.getComponentEnabledSetting(componentName));
    }
}