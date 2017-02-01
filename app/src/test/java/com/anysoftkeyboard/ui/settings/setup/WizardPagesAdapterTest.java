package com.anysoftkeyboard.ui.settings.setup;

import android.os.Build;

import com.anysoftkeyboard.ui.settings.MainSettingsActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class WizardPagesAdapterTest {

    WizardPagesAdapter mUnderTest;

    @Before
    public void setup() {
        MainSettingsActivity activity = Robolectric.setupActivity(MainSettingsActivity.class);
        mUnderTest = new WizardPagesAdapter(activity.getSupportFragmentManager());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testHasPermissionsPageForAndroidM() {
        Assert.assertEquals(4, mUnderTest.getCount());
        Assert.assertTrue(mUnderTest.getItem(2) instanceof WizardPermissionsFragment);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.JELLY_BEAN)
    public void testNoPermissionsPageBeforeAndroidM() {
        Assert.assertEquals(3, mUnderTest.getCount());
        Assert.assertFalse(mUnderTest.getItem(2) instanceof WizardPermissionsFragment);
    }
}