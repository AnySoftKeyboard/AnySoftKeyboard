package com.anysoftkeyboard.ui.settings.setup;

import android.content.ComponentName;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowSettings;

public class WizardPageSwitchToKeyboardFragmentTest extends RobolectricFragmentTestCase<WizardPageSwitchToKeyboardFragment> {

    @NonNull
    @Override
    protected WizardPageSwitchToKeyboardFragment createFragment() {
        return new WizardPageSwitchToKeyboardFragment();
    }

    @Test
    public void testKeyboardNotEnabled() {
        WizardPageSwitchToKeyboardFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(RuntimeEnvironment.application));

        ImageView stateIcon = (ImageView) fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertEquals(R.drawable.ic_wizard_switch_off, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
        Assert.assertTrue(stateIcon.isClickable());
    }

    @Test
    public void testKeyboardEnabledButNotDefault() {
        final String flatASKComponent = new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName()).flattenToString();
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS, flatASKComponent);

        WizardPageSwitchToKeyboardFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(RuntimeEnvironment.application));
        
        ImageView stateIcon = (ImageView) fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertEquals(R.drawable.ic_wizard_switch_off, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
        Assert.assertTrue(stateIcon.isClickable());

        View.OnClickListener stateIconClickHandler = Shadows.shadowOf(stateIcon).getOnClickListener();
        View.OnClickListener linkClickHandler = Shadows.shadowOf((View)fragment.getView().findViewById(R.id.go_to_switch_keyboard_action)).getOnClickListener();

        Assert.assertNotNull(stateIconClickHandler);
        Assert.assertSame(stateIconClickHandler, linkClickHandler);
    }

    @Test
    public void testKeyboardEnabledAndDefault() {
        final String flatASKComponent = new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName()).flattenToString();
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS, flatASKComponent);
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD, flatASKComponent);

        WizardPageSwitchToKeyboardFragment fragment = startFragment();
        Assert.assertTrue(fragment.isStepCompleted(RuntimeEnvironment.application));

        ImageView stateIcon = (ImageView) fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertEquals(R.drawable.ic_wizard_switch_on, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
        Assert.assertFalse(stateIcon.isClickable());
    }
}