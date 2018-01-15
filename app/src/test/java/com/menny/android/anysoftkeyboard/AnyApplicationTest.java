package com.menny.android.anysoftkeyboard;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AnyApplicationTest {

    @Test
    public void testSettingsAppIcon() {
        final PackageManager packageManager = RuntimeEnvironment.application.getPackageManager();
        final ComponentName componentName = new ComponentName(RuntimeEnvironment.application, LauncherSettingsActivity.class);

        Assert.assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED, packageManager.getComponentEnabledSetting(componentName));

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_show_settings_app, false);

        Assert.assertEquals(PackageManager.COMPONENT_ENABLED_STATE_DISABLED, packageManager.getComponentEnabledSetting(componentName));

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_show_settings_app, true);

        Assert.assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED, packageManager.getComponentEnabledSetting(componentName));
    }
}