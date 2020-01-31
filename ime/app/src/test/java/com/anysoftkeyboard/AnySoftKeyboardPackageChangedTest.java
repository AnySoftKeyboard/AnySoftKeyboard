package com.anysoftkeyboard;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.test.core.app.ApplicationProvider;
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
import org.robolectric.Shadows;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardPackageChangedTest {

    private AddOn mKeyboard;
    private AddOn mTheme;
    private AddOn mQuickTextKey;
    private TestableAnySoftKeyboard mSoftKeyboard;

    @Before
    public void setUp() throws Exception {
        mSoftKeyboard = Robolectric.buildService(TestableAnySoftKeyboard.class).create().get();
        mKeyboard = AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn();
        mTheme = AnyApplication.getKeyboardThemeFactory(getApplicationContext()).getEnabledAddOn();
        mQuickTextKey =
                AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getEnabledAddOn();
    }

    @Test
    public void testNoReloadOnEmptyBroadcast() throws Exception {
        ApplicationProvider.getApplicationContext().sendBroadcast(new Intent());
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertSame(
                mKeyboard,
                AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn());
        Assert.assertSame(
                mTheme,
                AnyApplication.getKeyboardThemeFactory(getApplicationContext()).getEnabledAddOn());
        Assert.assertSame(
                mQuickTextKey,
                AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getEnabledAddOn());
    }

    @Test
    public void testChangedPackageWithoutPackageName() throws Exception {
        // no add-on in changed package
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_CHANGED);
        ApplicationProvider.getApplicationContext().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertSame(
                mKeyboard,
                AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn());
        Assert.assertSame(
                mTheme,
                AnyApplication.getKeyboardThemeFactory(getApplicationContext()).getEnabledAddOn());
        Assert.assertSame(
                mQuickTextKey,
                AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getEnabledAddOn());
    }

    @Test
    public void testChangedPackageWithPackageNameManaged() throws Exception {
        // package with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_CHANGED);
        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        ApplicationProvider.getApplicationContext().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertNotSame(
                mKeyboard,
                AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn());
        Assert.assertNotSame(
                mTheme,
                AnyApplication.getKeyboardThemeFactory(getApplicationContext()).getEnabledAddOn());
        Assert.assertNotSame(
                mQuickTextKey,
                AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getEnabledAddOn());
    }

    @Test
    public void testChangedPackageWithPackageNameNotManagedManaged() throws Exception {
        // package with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_CHANGED);
        intent.setData(Uri.parse("package:" + "NOT_MANAGED"));
        ApplicationProvider.getApplicationContext().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertSame(
                mKeyboard,
                AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn());
        Assert.assertSame(
                mTheme,
                AnyApplication.getKeyboardThemeFactory(getApplicationContext()).getEnabledAddOn());
        Assert.assertSame(
                mQuickTextKey,
                AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getEnabledAddOn());
    }

    @Test
    public void testRemovedPackageWithPackageNameManaged() throws Exception {
        // package removed with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_REMOVED);
        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        ApplicationProvider.getApplicationContext().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertNotSame(
                mKeyboard,
                AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn());
        Assert.assertNotSame(
                mTheme,
                AnyApplication.getKeyboardThemeFactory(getApplicationContext()).getEnabledAddOn());
        Assert.assertNotSame(
                mQuickTextKey,
                AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getEnabledAddOn());
    }

    @Test
    public void testRemovedPackageWithPackageNameNotManaged() throws Exception {
        // package removed with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_REMOVED);
        intent.setData(Uri.parse("package:" + "NOT_MANAGED_PACKAGE"));
        ApplicationProvider.getApplicationContext().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertSame(
                mKeyboard,
                AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn());
        Assert.assertSame(
                mTheme,
                AnyApplication.getKeyboardThemeFactory(getApplicationContext()).getEnabledAddOn());
        Assert.assertSame(
                mQuickTextKey,
                AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getEnabledAddOn());
    }

    @Test
    public void testAddedPackageWithPackageName() throws Exception {
        final String NET_ADDONS_YES = "net.addons.yes";
        final PackageInfo packageInfoWithAddOns = new PackageInfo();
        packageInfoWithAddOns.packageName = NET_ADDONS_YES;
        packageInfoWithAddOns.receivers = new ActivityInfo[] {Mockito.spy(new ActivityInfo())};
        packageInfoWithAddOns.receivers[0].enabled = true;
        packageInfoWithAddOns.receivers[0].applicationInfo = new ApplicationInfo();
        packageInfoWithAddOns.receivers[0].applicationInfo.enabled = true;
        packageInfoWithAddOns.receivers[0].metaData = new Bundle();
        packageInfoWithAddOns.receivers[0].metaData.putInt(
                "com.menny.android.anysoftkeyboard.keyboards", R.xml.english_keyboards);
        Mockito.doReturn(getApplicationContext().getResources().getXml(R.xml.english_keyboards))
                .when(packageInfoWithAddOns.receivers[0])
                .loadXmlMetaData(
                        Mockito.any(), Mockito.eq("com.menny.android.anysoftkeyboard.keyboards"));
        Shadows.shadowOf(getApplicationContext().getPackageManager())
                .addPackage(packageInfoWithAddOns);

        // package added with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_ADDED);
        intent.setData(Uri.parse("package:" + NET_ADDONS_YES));
        ApplicationProvider.getApplicationContext().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertNotSame(
                mKeyboard,
                AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn());
    }

    @Test
    public void testAddedPackageWithPackageNameWithoutAddOns() throws Exception {
        final String NET_ADDONS_NO = "net.addons.no";
        final PackageInfo packageInfoWithoutAddOns = new PackageInfo();
        packageInfoWithoutAddOns.packageName = NET_ADDONS_NO;
        packageInfoWithoutAddOns.receivers = new ActivityInfo[] {new ActivityInfo()};
        packageInfoWithoutAddOns.receivers[0].enabled = true;
        packageInfoWithoutAddOns.receivers[0].applicationInfo = new ApplicationInfo();
        packageInfoWithoutAddOns.receivers[0].applicationInfo.enabled = true;
        packageInfoWithoutAddOns.receivers[0].metaData = new Bundle();
        Shadows.shadowOf(getApplicationContext().getPackageManager())
                .addPackage(packageInfoWithoutAddOns);

        // package added without addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_ADDED);
        intent.setData(Uri.parse("package:" + NET_ADDONS_NO));
        ApplicationProvider.getApplicationContext().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertSame(
                mKeyboard,
                AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn());
    }

    @Test
    public void testAddedPackageWithPackageNameWithDisabledAddOns() throws Exception {
        final String NET_ADDONS_YES = "net.addons.yes";
        final PackageInfo packageInfoWithAddOns = new PackageInfo();
        packageInfoWithAddOns.packageName = NET_ADDONS_YES;
        packageInfoWithAddOns.receivers = new ActivityInfo[] {new ActivityInfo()};
        packageInfoWithAddOns.receivers[0].enabled = false;
        packageInfoWithAddOns.receivers[0].metaData = new Bundle();
        packageInfoWithAddOns.receivers[0].metaData.putInt(
                "com.menny.android.anysoftkeyboard.keyboards", R.xml.english_keyboards);
        Shadows.shadowOf(getApplicationContext().getPackageManager())
                .addPackage(packageInfoWithAddOns);

        // package added with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_ADDED);
        intent.setData(Uri.parse("package:" + NET_ADDONS_YES));
        ApplicationProvider.getApplicationContext().sendBroadcast(intent);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        Assert.assertSame(
                mKeyboard,
                AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn());
    }

    @Test
    public void testClearsCachesAndDoesNotCreatesViewIfNeverCreated() throws Exception {
        mSoftKeyboard.getKeyboardSwitcherForTests().getEnabledKeyboardsBuilders();
        AnyKeyboard[] array =
                mSoftKeyboard.getKeyboardSwitcherForTests().getCachedAlphabetKeyboardsArray();
        Assert.assertNull(mSoftKeyboard.getInputView());

        mSoftKeyboard.onAddOnsCriticalChange();
        Assert.assertNotSame(
                array,
                mSoftKeyboard.getKeyboardSwitcherForTests().getCachedAlphabetKeyboardsArray());
        Assert.assertNull(mSoftKeyboard.getInputView());
    }
}
