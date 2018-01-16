package com.anysoftkeyboard;

import android.content.Intent;
import android.net.Uri;

import com.anysoftkeyboard.addons.AddOn;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.SoftKeyboard;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardPackageChangedTest {

    private AddOn mKeyboard;
    private AddOn mTheme;
    private AddOn mQuickTextKey;

    @Before
    public void setUp() throws Exception {
        Robolectric.buildService(SoftKeyboard.class).create();
        mKeyboard = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn();
        mTheme = AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn();
        mQuickTextKey = AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledAddOn();
    }

    @Test
    public void testNoReloadOnEmptyBroadcast() throws Exception {
        ShadowApplication.getInstance().sendBroadcast(new Intent());
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertSame(mKeyboard, AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertSame(mTheme, AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertSame(mQuickTextKey, AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledAddOn());
    }

    @Test
    public void testChangedPackageWithoutPackageName() throws Exception {
        //no add-on in changed package
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_CHANGED);
        ShadowApplication.getInstance().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertSame(mKeyboard, AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertSame(mTheme, AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertSame(mQuickTextKey, AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledAddOn());
    }

    @Test
    public void testChangedPackageWithPackageName() throws Exception {
        // package with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_CHANGED);
        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        ShadowApplication.getInstance().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertNotSame(mKeyboard, AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertNotSame(mTheme, AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertNotSame(mQuickTextKey, AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledAddOn());
    }

    @Test
    public void testRemovedPackageWithPackageName() throws Exception {
        // package removed with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_REMOVED);
        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        ShadowApplication.getInstance().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertNotSame(mKeyboard, AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertNotSame(mTheme, AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertNotSame(mQuickTextKey, AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledAddOn());
    }

    @Test
    @Ignore("Seems like Robolecrtic does not read receivers from manifest")
    public void testAddedPackageWithPackageName() throws Exception {
        // package added with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_ADDED);
        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        ShadowApplication.getInstance().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertNotSame(mKeyboard, AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertNotSame(mTheme, AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertNotSame(mQuickTextKey, AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledAddOn());
    }
}
