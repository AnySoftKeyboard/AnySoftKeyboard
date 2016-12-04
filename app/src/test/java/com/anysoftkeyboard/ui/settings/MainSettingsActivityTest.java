package com.anysoftkeyboard.ui.settings;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class MainSettingsActivityTest {


    @Test(expected = IllegalArgumentException.class)
    public void testUnknownAppShortcut() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, createAppShortcutIntent("unknown_id"));
        activityController.setup();
    }

    @Test
    public void testNoAppShortcutExtra() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class);
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof MainFragment);
    }

    @Test
    public void testNoAppShortcutExtraButWithIntent() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, new Intent(RuntimeEnvironment.application, MainSettingsActivity.class));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof MainFragment);
    }

    @Test
    public void testKeyboardsAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, createAppShortcutIntent("keyboards"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof KeyboardAddOnBrowserFragment);

        Assert.assertFalse(activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    public void testThemesAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, createAppShortcutIntent("themes"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof KeyboardThemeSelectorFragment);

        Assert.assertFalse(activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    public void testGesturesAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, createAppShortcutIntent("gestures"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof GesturesSettingsFragment);

        Assert.assertFalse(activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    public void testQuickKeysAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, createAppShortcutIntent("quick_keys"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof QuickTextSettingsFragment);

        Assert.assertFalse(activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    public void testUiAdditionalAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, createAppShortcutIntent("ui_tweaks"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof AdditionalUiSettingsFragment);

        Assert.assertFalse(activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    private static Intent createAppShortcutIntent(String shortcutId) {
        Intent intent = new Intent(Intent.ACTION_VIEW, null, RuntimeEnvironment.application, MainSettingsActivity.class);
        intent.putExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID, shortcutId);

        return intent;
    }
}