package com.anysoftkeyboard;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardPackageChangedTest {

    private AddOn mKeyboard;
    private AddOn mTheme;
    private AddOn mQuickTextKey;
    private TestableAnySoftKeyboard mSoftKeyboard;

    @Before
    public void setUp() throws Exception {
        mSoftKeyboard = Robolectric.buildService(TestableAnySoftKeyboard.class).create().get();
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
    public void testChangedPackageWithPackageNameManaged() throws Exception {
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
    public void testChangedPackageWithPackageNameNotManagedManaged() throws Exception {
        // package with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_CHANGED);
        intent.setData(Uri.parse("package:" + "NOT_MANAGED"));
        ShadowApplication.getInstance().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertSame(mKeyboard, AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertSame(mTheme, AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertSame(mQuickTextKey, AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledAddOn());
    }

    @Test
    public void testRemovedPackageWithPackageNameManaged() throws Exception {
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
    public void testRemovedPackageWithPackageNameNotManaged() throws Exception {
        // package removed with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_REMOVED);
        intent.setData(Uri.parse("package:" + "NOT_MANAGED_PACKAGE"));
        ShadowApplication.getInstance().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertSame(mKeyboard, AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertSame(mTheme, AnyApplication.getKeyboardThemeFactory(RuntimeEnvironment.application).getEnabledAddOn());
        Assert.assertSame(mQuickTextKey, AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getEnabledAddOn());
    }

    @Test
    public void testAddedPackageWithPackageName() throws Exception {
        final String NET_ADDONS_YES = "net.addons.yes";
        final PackageInfo packageInfoWithAddOns = new PackageInfo();
        packageInfoWithAddOns.packageName = NET_ADDONS_YES;
        packageInfoWithAddOns.receivers = new ActivityInfo[]{Mockito.spy(new ActivityInfo())};
        packageInfoWithAddOns.receivers[0].enabled = true;
        packageInfoWithAddOns.receivers[0].applicationInfo = new ApplicationInfo();
        packageInfoWithAddOns.receivers[0].applicationInfo.enabled = true;
        packageInfoWithAddOns.receivers[0].metaData = new Bundle();
        packageInfoWithAddOns.receivers[0].metaData.putInt("com.menny.android.anysoftkeyboard.keyboards", R.xml.keyboards);
        Mockito.doReturn(RuntimeEnvironment.application.getResources().getXml(R.xml.keyboards))
                .when(packageInfoWithAddOns.receivers[0]).loadXmlMetaData(Mockito.any(), Mockito.eq("com.menny.android.anysoftkeyboard.keyboards"));
        Shadows.shadowOf(RuntimeEnvironment.application.getPackageManager()).addPackage(packageInfoWithAddOns);

        // package added with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_ADDED);
        intent.setData(Uri.parse("package:" + NET_ADDONS_YES));
        ShadowApplication.getInstance().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertNotSame(mKeyboard, AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn());
    }

    @Test
    public void testAddedPackageWithPackageNameWithoutAddOns() throws Exception {
        final String NET_ADDONS_NO = "net.addons.no";
        final PackageInfo packageInfoWithoutAddOns = new PackageInfo();
        packageInfoWithoutAddOns.packageName = NET_ADDONS_NO;
        packageInfoWithoutAddOns.receivers = new ActivityInfo[]{new ActivityInfo()};
        packageInfoWithoutAddOns.receivers[0].enabled = true;
        packageInfoWithoutAddOns.receivers[0].applicationInfo = new ApplicationInfo();
        packageInfoWithoutAddOns.receivers[0].applicationInfo.enabled = true;
        packageInfoWithoutAddOns.receivers[0].metaData = new Bundle();
        Shadows.shadowOf(RuntimeEnvironment.application.getPackageManager()).addPackage(packageInfoWithoutAddOns);

        // package added without addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_ADDED);
        intent.setData(Uri.parse("package:" + NET_ADDONS_NO));
        ShadowApplication.getInstance().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertSame(mKeyboard, AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn());
    }

    @Test
    public void testAddedPackageWithPackageNameWithDisabledAddOns() throws Exception {
        final String NET_ADDONS_YES = "net.addons.yes";
        final PackageInfo packageInfoWithAddOns = new PackageInfo();
        packageInfoWithAddOns.packageName = NET_ADDONS_YES;
        packageInfoWithAddOns.receivers = new ActivityInfo[]{new ActivityInfo()};
        packageInfoWithAddOns.receivers[0].enabled = false;
        packageInfoWithAddOns.receivers[0].metaData = new Bundle();
        packageInfoWithAddOns.receivers[0].metaData.putInt("com.menny.android.anysoftkeyboard.keyboards", R.xml.keyboards);
        Shadows.shadowOf(RuntimeEnvironment.application.getPackageManager()).addPackage(packageInfoWithAddOns);

        // package added with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_ADDED);
        intent.setData(Uri.parse("package:" + NET_ADDONS_YES));
        ShadowApplication.getInstance().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertSame(mKeyboard, AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn());
    }

    @Test
    public void testClearsCachesAndDoesNotCreatesViewIfNeverCreated() throws Exception {
        mSoftKeyboard.getKeyboardSwitcherForTests().getEnabledKeyboardsBuilders();
        AnyKeyboard[] array = mSoftKeyboard.getKeyboardSwitcherForTests().getCachedAlphabetKeyboardsArray();
        Assert.assertNull(mSoftKeyboard.getInputView());

        mSoftKeyboard.onAddOnsCriticalChange();
        Assert.assertNotSame(array, mSoftKeyboard.getKeyboardSwitcherForTests().getCachedAlphabetKeyboardsArray());
        Assert.assertNull(mSoftKeyboard.getInputView());
    }
}
