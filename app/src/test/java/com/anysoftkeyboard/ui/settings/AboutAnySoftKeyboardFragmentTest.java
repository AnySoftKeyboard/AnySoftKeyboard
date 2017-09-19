package com.anysoftkeyboard.ui.settings;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class AboutAnySoftKeyboardFragmentTest extends RobolectricFragmentTestCase<AboutAnySoftKeyboardFragment> {

    @NonNull
    @Override
    protected AboutAnySoftKeyboardFragment createFragment() {
        return new AboutAnySoftKeyboardFragment();
    }

    @Test
    public void testWebSiteClick() {
        AboutAnySoftKeyboardFragment fragment = startFragment();
        TextView link = fragment.getView().findViewById(R.id.about_web_site_link);
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
        TextView link = fragment.getView().findViewById(R.id.about_privacy_link);
        Assert.assertNotNull(link);

        link.performClick();

        Intent intent = ShadowApplication.getInstance().getNextStartedActivity();

        Assert.assertNotNull(intent);
        Assert.assertEquals(Intent.ACTION_VIEW, intent.getAction());
        Assert.assertEquals("https://anysoftkeyboard.github.io/privacy_policy.html", intent.getData().toString());
    }

    @Test
    public void testAdditionalLicenses() {
        AboutAnySoftKeyboardFragment fragment = startFragment();
        TextView link = fragment.getView().findViewById(R.id.about_legal_stuff_link);
        Assert.assertNotNull(link);

        Shadows.shadowOf(link).getOnClickListener().onClick(link);

        ensureAllScheduledJobsAreDone();

        Fragment nextFragment = fragment.getFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(nextFragment);
        Assert.assertTrue(nextFragment instanceof AboutAnySoftKeyboardFragment.AdditionalSoftwareLicensesFragment);
    }

    @Test
    public void testVersionInfo() {
        AboutAnySoftKeyboardFragment fragment = startFragment();
        TextView copyright = fragment.getView().findViewById(R.id.about_copyright);
        Assert.assertTrue(copyright.getText().toString().contains("Menny"));
        Assert.assertTrue(copyright.getText().toString().contains("©"));
        Assert.assertTrue(copyright.getText().toString().contains(Integer.toString(new GregorianCalendar().get(Calendar.YEAR))));

        TextView version = fragment.getView().findViewById(R.id.about_app_version);
        Assert.assertTrue(version.getText().toString().contains(BuildConfig.VERSION_NAME));
        Assert.assertTrue(version.getText().toString().contains(Integer.toString(BuildConfig.VERSION_CODE)));
    }
}