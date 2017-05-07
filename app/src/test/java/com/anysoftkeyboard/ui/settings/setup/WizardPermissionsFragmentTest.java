package com.anysoftkeyboard.ui.settings.setup;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowSettings;

@Config(sdk = Build.VERSION_CODES.M)
public class WizardPermissionsFragmentTest extends RobolectricFragmentTestCase<WizardPermissionsFragment> {

    @NonNull
    @Override
    protected WizardPermissionsFragment createFragment() {
        return new WizardPermissionsFragment();
    }

    @Test
    public void testKeyboardNotEnabled() {
        WizardPermissionsFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(RuntimeEnvironment.application));
        Assert.assertFalse(fragment.isStepPreConditionDone(RuntimeEnvironment.application));

        Assert.assertEquals(View.VISIBLE, fragment.getView().findViewById(R.id.previous_step_not_complete).getVisibility());
        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.this_step_complete).getVisibility());
        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.this_step_needs_setup).getVisibility());

        ImageView stateIcon = (ImageView) fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertEquals(R.drawable.ic_wizard_contacts_off, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
        Assert.assertFalse(stateIcon.isClickable());
    }

    @Test
    public void testKeyboardEnabledButNotDefault() {
        final String flatASKComponent = new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName()).flattenToString();
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS, flatASKComponent);

        WizardPermissionsFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(RuntimeEnvironment.application));
        Assert.assertFalse(fragment.isStepPreConditionDone(RuntimeEnvironment.application));

        Assert.assertEquals(View.VISIBLE, fragment.getView().findViewById(R.id.previous_step_not_complete).getVisibility());
        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.this_step_complete).getVisibility());
        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.this_step_needs_setup).getVisibility());

        ImageView stateIcon = (ImageView) fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertEquals(R.drawable.ic_wizard_contacts_off, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
        Assert.assertFalse(stateIcon.isClickable());
    }

    @Test
    public void testKeyboardEnabledAndDefaultButNoPermission() {
        final String flatASKComponent = new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName()).flattenToString();
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS, flatASKComponent);
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD, flatASKComponent);

        WizardPermissionsFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(RuntimeEnvironment.application));
        Assert.assertTrue(fragment.isStepPreConditionDone(RuntimeEnvironment.application));

        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.previous_step_not_complete).getVisibility());
        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.this_step_complete).getVisibility());
        Assert.assertEquals(View.VISIBLE, fragment.getView().findViewById(R.id.this_step_needs_setup).getVisibility());

        ImageView stateIcon = (ImageView) fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertEquals(R.drawable.ic_wizard_contacts_off, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
        Assert.assertTrue(stateIcon.isClickable());
        //can handle wiki?
        fragment.getView().findViewById(R.id.open_permissions_wiki_action).performClick();
        Intent wikiIntent = ShadowApplication.getInstance().getNextStartedActivity();
        Assert.assertEquals(Intent.ACTION_VIEW, wikiIntent.getAction());
        Assert.assertEquals("https://github.com/AnySoftKeyboard/AnySoftKeyboard/wiki/Why-Does-AnySoftKeyboard-Requires-Extra-Permissions", wikiIntent.getData().toString());
        //can disable Contacts
        fragment.getView().findViewById(R.id.disable_contacts_dictionary).performClick();
        Assert.assertFalse(AnyApplication.getConfig().useContactsDictionary());
        Assert.assertEquals(R.drawable.ic_wizard_contacts_disabled, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
    }

    @Test
    public void testKeyboardEnabledAndDefaultButDictionaryDisabled() {
        final String flatASKComponent = new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName()).flattenToString();
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS, flatASKComponent);
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD, flatASKComponent);
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_use_contacts_dictionary, false);

        WizardPermissionsFragment fragment = startFragment();
        Assert.assertTrue(fragment.isStepCompleted(RuntimeEnvironment.application));
        Assert.assertTrue(fragment.isStepPreConditionDone(RuntimeEnvironment.application));

        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.previous_step_not_complete).getVisibility());
        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.this_step_complete).getVisibility());
        Assert.assertEquals(View.VISIBLE, fragment.getView().findViewById(R.id.this_step_needs_setup).getVisibility());

        ImageView stateIcon = (ImageView) fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertEquals(R.drawable.ic_wizard_contacts_disabled, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
        Assert.assertTrue(stateIcon.isClickable());

        //now, clicking on ALLOW, should enable the dictionary back and start permission request
        stateIcon.performClick();
        Assert.assertTrue(AnyApplication.getConfig().useContactsDictionary());
    }

    @Test
    public void testKeyboardEnabledAndDefaultAndHasPermission() {
        final String flatASKComponent = new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName()).flattenToString();
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS, flatASKComponent);
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD, flatASKComponent);
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.READ_CONTACTS);

        WizardPermissionsFragment fragment = startFragment();
        Assert.assertTrue(fragment.isStepCompleted(RuntimeEnvironment.application));
        Assert.assertTrue(fragment.isStepPreConditionDone(RuntimeEnvironment.application));

        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.previous_step_not_complete).getVisibility());
        Assert.assertEquals(View.VISIBLE, fragment.getView().findViewById(R.id.this_step_complete).getVisibility());
        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.this_step_needs_setup).getVisibility());

        ImageView stateIcon = (ImageView) fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertEquals(R.drawable.ic_wizard_contacts_on, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
        Assert.assertFalse(stateIcon.isClickable());

        View.OnClickListener stateIconClickHandler = Shadows.shadowOf(stateIcon).getOnClickListener();
        View.OnClickListener linkClickHandler = Shadows.shadowOf(fragment.getView().findViewById(R.id.ask_for_permissions_action)).getOnClickListener();

        Assert.assertNotNull(stateIconClickHandler);
        Assert.assertSame(stateIconClickHandler, linkClickHandler);
    }
}