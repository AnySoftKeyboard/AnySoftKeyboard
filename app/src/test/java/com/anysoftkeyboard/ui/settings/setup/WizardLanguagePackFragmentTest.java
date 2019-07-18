package com.anysoftkeyboard.ui.settings.setup;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;
import java.util.Locale;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Shadows;

public class WizardLanguagePackFragmentTest
        extends RobolectricFragmentTestCase<WizardLanguagePackFragment> {

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
        Assert.assertTrue(fragment.isStepCompleted(getApplicationContext()));

        ImageView stateIcon = fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertFalse(stateIcon.isClickable());
        Assert.assertEquals(
                R.drawable.ic_wizard_download_pack_ready,
                Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
    }

    @Test
    public void testHappyPath() {
        Locale.setDefault(Locale.FRANCE);
        WizardLanguagePackFragment fragment = startFragment();
        Assert.assertFalse(fragment.isStepCompleted(getApplicationContext()));

        ImageView stateIcon = fragment.getView().findViewById(R.id.step_state_icon);
        Assert.assertNotNull(stateIcon);

        Assert.assertTrue(stateIcon.isClickable());
        Assert.assertEquals(
                R.drawable.ic_wizard_download_pack_missing,
                Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());

        View.OnClickListener stateIconClickHandler =
                Shadows.shadowOf(stateIcon).getOnClickListener();
        View.OnClickListener linkClickHandler =
                Shadows.shadowOf(
                                (View)
                                        fragment.getView()
                                                .findViewById(R.id.go_to_download_packs_action))
                        .getOnClickListener();

        Assert.assertNotNull(stateIconClickHandler);
        Assert.assertSame(stateIconClickHandler, linkClickHandler);

        Assert.assertNull(
                Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                        .getNextStartedActivity());

        stateIconClickHandler.onClick(null);

        Intent searchIntent =
                Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                        .getNextStartedActivity();
        Assert.assertNotNull(searchIntent);
    }
}
