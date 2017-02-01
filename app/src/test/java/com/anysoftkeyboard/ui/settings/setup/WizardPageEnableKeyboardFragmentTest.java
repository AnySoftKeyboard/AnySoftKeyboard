package com.anysoftkeyboard.ui.settings.setup;

import android.content.ComponentName;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowSettings;

public class WizardPageEnableKeyboardFragmentTest extends com.anysoftkeyboard.RobolectricFragmentTestCase<WizardPageEnableKeyboardFragment> {

    @NonNull
    @Override
    protected WizardPageEnableKeyboardFragment createFragment() {
        return new WizardPageEnableKeyboardFragment();
    }

    @Test
    public void testKeyboardNotEnabled() {
        WizardPageEnableKeyboardFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(RuntimeEnvironment.application));
        Assert.assertTrue(fragment.isStepPreConditionDone(RuntimeEnvironment.application));

        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.previous_step_not_complete).getVisibility());
        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.this_step_complete).getVisibility());
        Assert.assertEquals(View.VISIBLE, fragment.getView().findViewById(R.id.this_step_needs_setup).getVisibility());

        ImageView stateIcon = (ImageView) fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertEquals(R.drawable.ic_wizard_enabled_off, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
        Assert.assertTrue(stateIcon.isClickable());

        View.OnClickListener stateIconClickHandler = Shadows.shadowOf(stateIcon).getOnClickListener();
        View.OnClickListener linkClickHandler = Shadows.shadowOf(fragment.getView().findViewById(R.id.go_to_language_settings_action)).getOnClickListener();

        Assert.assertNotNull(stateIconClickHandler);
        Assert.assertSame(stateIconClickHandler, linkClickHandler);
    }

    @Test
    public void testKeyboardEnabled() {
        final String flatASKComponent = new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName()).flattenToString();
        ShadowSettings.ShadowSecure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS, flatASKComponent);

        WizardPageEnableKeyboardFragment fragment = startFragment();
        Assert.assertTrue(fragment.isStepCompleted(RuntimeEnvironment.application));
        Assert.assertTrue(fragment.isStepPreConditionDone(RuntimeEnvironment.application));

        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.previous_step_not_complete).getVisibility());
        Assert.assertEquals(View.VISIBLE, fragment.getView().findViewById(R.id.this_step_complete).getVisibility());
        Assert.assertEquals(View.GONE, fragment.getView().findViewById(R.id.this_step_needs_setup).getVisibility());

        ImageView stateIcon = (ImageView) fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertEquals(R.drawable.ic_wizard_enabled_on, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
        Assert.assertFalse(stateIcon.isClickable());
    }
}