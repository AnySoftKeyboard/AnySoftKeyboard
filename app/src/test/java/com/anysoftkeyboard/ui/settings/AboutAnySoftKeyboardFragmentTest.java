package com.anysoftkeyboard.ui.settings;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

public class AboutAnySoftKeyboardFragmentTest extends RobolectricFragmentTestCase<AboutAnySoftKeyboardFragment> {

    @NonNull
    @Override
    protected AboutAnySoftKeyboardFragment createFragment() {
        return new AboutAnySoftKeyboardFragment();
    }

    @Test
    public void testWebSiteClick() {
        AboutAnySoftKeyboardFragment fragment = startFragment();
        TextView link = (TextView) fragment.getView().findViewById(R.id.about_web_site_link);
        Assert.assertNotNull(link);

        Shadows.shadowOf(link).checkedPerformClick();

        Intent intent = ShadowApplication.getInstance().getNextStartedActivity();

        Assert.assertNotNull(intent);
        Assert.assertEquals(Intent.ACTION_VIEW, intent.getAction());
        Assert.assertEquals("https://anysoftkeyboard.github.io/", intent.getData().toString());
    }

    @Test
    public void testPrivacyPolicyClick() {
        AboutAnySoftKeyboardFragment fragment = startFragment();
        TextView link = (TextView) fragment.getView().findViewById(R.id.about_privacy_link);
        Assert.assertNotNull(link);

        link.performClick();

        Intent intent = ShadowApplication.getInstance().getNextStartedActivity();

        Assert.assertNotNull(intent);
        Assert.assertEquals(Intent.ACTION_VIEW, intent.getAction());
        Assert.assertEquals("https://raw.githubusercontent.com/AnySoftKeyboard/AnySoftKeyboard/master/StoreStuff/privacy_policy.html", intent.getData().toString());
    }

    @Test
    public void testAdditionalLicenses() {
        AboutAnySoftKeyboardFragment fragment = startFragment();
        TextView link = (TextView) fragment.getView().findViewById(R.id.about_legal_stuff_link);
        Assert.assertNotNull(link);

        Shadows.shadowOf(link).getOnClickListener().onClick(link);

        ensureAllScheduledJobsAreDone();

        Fragment nextFragment = fragment.getFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(nextFragment);
        Assert.assertTrue(nextFragment instanceof AboutAnySoftKeyboardFragment.AdditionalSoftwareLicensesFragment);
    }
}