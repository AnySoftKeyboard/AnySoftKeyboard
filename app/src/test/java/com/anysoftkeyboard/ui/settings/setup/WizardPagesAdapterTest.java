package com.anysoftkeyboard.ui.settings.setup;

import android.os.Build;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardTestRunner.class)
public class WizardPagesAdapterTest {

    private MainSettingsActivity mActivity;

    @Before
    public void setup() {
        mActivity = Robolectric.setupActivity(MainSettingsActivity.class);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testHasPermissionsPageForAndroidM() {
        WizardPagesAdapter adapter = new WizardPagesAdapter(mActivity.getSupportFragmentManager(), false);

        Assert.assertEquals(5, adapter.getCount());
        Assert.assertTrue(adapter.getItem(3) instanceof WizardPermissionsFragment);

        adapter = new WizardPagesAdapter(mActivity.getSupportFragmentManager(), true);
        Assert.assertEquals(6, adapter.getCount());
        Assert.assertTrue(adapter.getItem(3) instanceof WizardPermissionsFragment);
        Assert.assertTrue(adapter.getItem(4) instanceof WizardLanguagePackFragment);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.JELLY_BEAN)
    public void testNoPermissionsPageBeforeAndroidM() {
        WizardPagesAdapter adapter = new WizardPagesAdapter(mActivity.getSupportFragmentManager(), false);

        Assert.assertEquals(4, adapter.getCount());
        for (int fragmentIndex=0; fragmentIndex<adapter.getCount(); fragmentIndex++) {
            Assert.assertFalse(adapter.getItem(fragmentIndex) instanceof WizardPermissionsFragment);
        }
    }
}