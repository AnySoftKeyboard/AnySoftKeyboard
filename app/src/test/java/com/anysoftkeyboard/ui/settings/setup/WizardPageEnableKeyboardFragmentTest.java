package com.anysoftkeyboard.ui.settings.setup;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

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
import org.robolectric.Shadows;

@SuppressWarnings("cast")
public class WizardPageEnableKeyboardFragmentTest extends RobolectricFragmentTestCase<WizardPageEnableKeyboardFragment> {

    @NonNull
    @Override
    protected WizardPageEnableKeyboardFragment createFragment() {
        return new WizardPageEnableKeyboardFragment();
    }

    @Test
    public void testKeyboardNotEnabled() {
        WizardPageEnableKeyboardFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(getApplicationContext()));

        ImageView stateIcon = fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertEquals(R.drawable.ic_wizard_enabled_off, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
        Assert.assertTrue(stateIcon.isClickable());

        View.OnClickListener stateIconClickHandler = Shadows.shadowOf(stateIcon).getOnClickListener();
        View.OnClickListener linkClickHandler = Shadows.shadowOf((View) fragment.getView().findViewById(R.id.go_to_language_settings_action)).getOnClickListener();

        Assert.assertNotNull(stateIconClickHandler);
        Assert.assertSame(stateIconClickHandler, linkClickHandler);
    }

    @Test
    public void testKeyboardEnabled() {
        final String flatASKComponent = new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName()).flattenToString();
        Settings.Secure.putString(getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS, flatASKComponent);

        WizardPageEnableKeyboardFragment fragment = startFragment();
        Assert.assertTrue(fragment.isStepCompleted(getApplicationContext()));

        ImageView stateIcon = fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertEquals(R.drawable.ic_wizard_enabled_on, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
        Assert.assertFalse(stateIcon.isClickable());
    }
}