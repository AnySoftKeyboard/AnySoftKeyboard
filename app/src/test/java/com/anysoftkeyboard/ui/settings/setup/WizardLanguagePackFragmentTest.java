package com.anysoftkeyboard.ui.settings.setup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

import java.util.Locale;

public class WizardLanguagePackFragmentTest extends RobolectricFragmentTestCase<WizardLanguagePackFragment> {

    @After
    public void tearDownLanguagePack() {
        Locale.setDefault(Locale.US);
    }

    @NonNull
    @Override
    protected WizardLanguagePackFragment createFragment() {
        return new WizardLanguagePackFragment();
    }

    @Test
    public void testPageCompleteIfStartedWithLanguagePackInstalled() {
        Locale.setDefault(Locale.US);
        WizardLanguagePackFragment fragment = startFragment();
        Assert.assertTrue(fragment.isStepCompleted(RuntimeEnvironment.application));

        ImageView stateIcon = (ImageView) fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertFalse(stateIcon.isClickable());
        Assert.assertEquals(R.drawable.ic_wizard_download_pack_ready, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
    }

    @Test
    public void testHappyPath() {
        Locale.setDefault(Locale.FRANCE);
        WizardLanguagePackFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(RuntimeEnvironment.application));

        ImageView stateIcon = (ImageView) fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertTrue(stateIcon.isClickable());
        Assert.assertEquals(R.drawable.ic_wizard_download_pack_missing, Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());

        View.OnClickListener stateIconClickHandler = Shadows.shadowOf(stateIcon).getOnClickListener();
        View.OnClickListener linkClickHandler = Shadows.shadowOf((View) fragment.getView().findViewById(R.id.go_to_download_packs_action)).getOnClickListener();

        Assert.assertNotNull(stateIconClickHandler);
        Assert.assertSame(stateIconClickHandler, linkClickHandler);

        Assert.assertNull(ShadowApplication.getInstance().getNextStartedActivity());

        stateIconClickHandler.onClick(null);

        Intent searchIntent = ShadowApplication.getInstance().getNextStartedActivity();
        Assert.assertNotNull(searchIntent);
    }
}