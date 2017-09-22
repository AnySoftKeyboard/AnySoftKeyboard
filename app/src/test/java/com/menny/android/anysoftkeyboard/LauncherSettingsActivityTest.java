package com.menny.android.anysoftkeyboard;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.ui.settings.BasicAnyActivity;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowSettings;

@RunWith(AnySoftKeyboardTestRunner.class)
public class LauncherSettingsActivityTest {

    @Test
    public void testOnCreateWhenASKNotEnabled() throws Exception {
        //mocking ASK as disabled and inactive
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(),
                Settings.Secure.ENABLED_INPUT_METHODS, new ComponentName("net.some.one.else", "net.some.one.else.IME").flattenToString());
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD, new ComponentName("net.some.one.else", "net.some.one.else.IME").flattenToString());

        Assert.assertNull(ShadowApplication.getInstance().getNextStartedActivity());
        Robolectric.buildActivity(LauncherSettingsActivity.class).create().resume();
        Intent startWizardActivityIntent = ShadowApplication.getInstance().getNextStartedActivity();
        Assert.assertNotNull(startWizardActivityIntent);

        Intent expectIntent = new Intent(RuntimeEnvironment.application, BasicAnyActivity.class);

        Assert.assertEquals(expectIntent.getComponent(), startWizardActivityIntent.getComponent());
        Assert.assertEquals(expectIntent.getAction(), startWizardActivityIntent.getAction());
        Assert.assertFalse(startWizardActivityIntent.hasExtra("FragmentChauffeurActivity_KEY_FRAGMENT_CLASS_TO_ADD"));
    }

    @Test
    public void testOnCreateWhenASKEnabledAndActive() throws Exception {
        //mocking ASK as enable and inactive
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(),
                Settings.Secure.ENABLED_INPUT_METHODS,
                new ComponentName("net.some.one.else", "net.some.one.else.IME").flattenToString() + ":" +
                        new ComponentName(RuntimeEnvironment.application.getPackageName(), RuntimeEnvironment.application.getPackageName() + ".IME").flattenToString());
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD, new ComponentName(RuntimeEnvironment.application.getPackageName(), RuntimeEnvironment.application.getPackageName() + ".IME").flattenToString());

        Assert.assertNull(ShadowApplication.getInstance().getNextStartedActivity());
        ActivityController<LauncherSettingsActivity> controller = Robolectric.buildActivity(LauncherSettingsActivity.class).create().resume();
        Intent startMainApp = ShadowApplication.getInstance().getNextStartedActivity();
        Assert.assertNotNull(startMainApp);

        Intent expectIntent = new Intent(controller.get(), MainSettingsActivity.class);

        Assert.assertEquals(expectIntent.getComponent(), startMainApp.getComponent());
        Assert.assertFalse(startMainApp.hasExtra("FragmentChauffeurActivity_KEY_FRAGMENT_CLASS_TO_ADD"));

    }

    @Test
    public void testOnCreateWhenASKEnabledAndInactive() throws Exception {
        //mocking ASK as enable and inactive
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(),
                Settings.Secure.ENABLED_INPUT_METHODS,
                new ComponentName("net.some.one.else", "net.some.one.else.IME").flattenToString() + ":" +
                        new ComponentName(RuntimeEnvironment.application.getPackageName(), RuntimeEnvironment.application.getPackageName() + ".IME").flattenToString());
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD, new ComponentName("net.some.one.else", "net.some.one.else.IME").flattenToString());

        Assert.assertNull(ShadowApplication.getInstance().getNextStartedActivity());
        ActivityController<LauncherSettingsActivity> controller = Robolectric.buildActivity(LauncherSettingsActivity.class).create().resume();
        Intent startMainApp = ShadowApplication.getInstance().getNextStartedActivity();
        Assert.assertNotNull(startMainApp);

        Intent expectIntent = new Intent(controller.get(), MainSettingsActivity.class);

        Assert.assertEquals(expectIntent.getComponent(), startMainApp.getComponent());
        Assert.assertFalse(startMainApp.hasExtra("FragmentChauffeurActivity_KEY_FRAGMENT_CLASS_TO_ADD"));
    }

    @Test
    public void testJustFinishIfResumedAgain() throws Exception {
        ActivityController<LauncherSettingsActivity> controller = Robolectric.buildActivity(LauncherSettingsActivity.class).create().resume();
        ShadowActivity shadowActivity = Shadows.shadowOf(controller.get());
        Assert.assertFalse(shadowActivity.isFinishing());
        controller.pause().stop();
        Assert.assertFalse(shadowActivity.isFinishing());
        controller.restart().resume();
        Assert.assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void testJustFinishIfCreatedAgain() throws Exception {
        ActivityController<LauncherSettingsActivity> controller = Robolectric.buildActivity(LauncherSettingsActivity.class).create().resume();
        ShadowActivity shadowActivity = Shadows.shadowOf(controller.get());
        Assert.assertFalse(shadowActivity.isFinishing());
        controller.pause().stop();
        Assert.assertFalse(shadowActivity.isFinishing());
        Bundle state = new Bundle();
        controller.saveInstanceState(state).destroy();

        controller = Robolectric.buildActivity(LauncherSettingsActivity.class).create(state);
        shadowActivity = Shadows.shadowOf(controller.get());
        Assert.assertFalse(shadowActivity.isFinishing());
        controller.resume();
        Assert.assertTrue(shadowActivity.isFinishing());
    }
}