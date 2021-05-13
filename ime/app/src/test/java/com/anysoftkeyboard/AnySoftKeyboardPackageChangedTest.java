package com.anysoftkeyboard;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowApplicationPackageManager;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(shadows = AnySoftKeyboardPackageChangedTest.ShadowPackageManagerWithXml.class)
public class AnySoftKeyboardPackageChangedTest {
    private static final String NET_ADDONS_YES = "net.addons.yes";

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
        final PackageInfo packageInfoWithAddOns = new PackageInfo();
        packageInfoWithAddOns.packageName = NET_ADDONS_YES;
        packageInfoWithAddOns.applicationInfo = new ApplicationInfo();
        packageInfoWithAddOns.applicationInfo.name = NET_ADDONS_YES;
        packageInfoWithAddOns.receivers = new ActivityInfo[] {new ActivityInfo()};
        packageInfoWithAddOns.receivers[0].enabled = true;
        packageInfoWithAddOns.receivers[0].packageName = NET_ADDONS_YES;
        packageInfoWithAddOns.receivers[0].applicationInfo = packageInfoWithAddOns.applicationInfo;
        packageInfoWithAddOns.receivers[0].applicationInfo.enabled = true;
        packageInfoWithAddOns.receivers[0].metaData = new Bundle();
        packageInfoWithAddOns.receivers[0].metaData.putInt(
                "com.menny.android.anysoftkeyboard.keyboards", R.xml.english_keyboards);
        Shadows.shadowOf(getApplicationContext().getPackageManager())
                .installPackage(packageInfoWithAddOns);
        // package added with addon
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_PACKAGE_ADDED);
        intent.setData(Uri.parse("package:" + NET_ADDONS_YES));
        ApplicationProvider.getApplicationContext().sendBroadcast(intent);
        TestRxSchedulers.drainAllTasks();
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
                .installPackage(packageInfoWithoutAddOns);

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
        final PackageInfo packageInfoWithAddOns = new PackageInfo();
        packageInfoWithAddOns.packageName = NET_ADDONS_YES;
        packageInfoWithAddOns.receivers = new ActivityInfo[] {new ActivityInfo()};
        packageInfoWithAddOns.receivers[0].enabled = false;
        packageInfoWithAddOns.receivers[0].metaData = new Bundle();
        packageInfoWithAddOns.receivers[0].metaData.putInt(
                "com.menny.android.anysoftkeyboard.keyboards", R.xml.english_keyboards);
        Shadows.shadowOf(getApplicationContext().getPackageManager())
                .installPackage(packageInfoWithAddOns);

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

    @Implements(className = "android.app.ApplicationPackageManager")
    public static class ShadowPackageManagerWithXml extends ShadowApplicationPackageManager {
        @RealObject PackageManager mManager;

        public ShadowPackageManagerWithXml() {}

        @Implementation
        public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
            if (!packageName.equals(ApplicationProvider.getApplicationContext().getPackageName())) {
                return ApplicationProvider.getApplicationContext().getResources().getXml(resid);
            } else {
                return Shadow.directlyOn(
                        mManager,
                        "android.app.ApplicationPackageManager",
                        "getXml",
                        ReflectionHelpers.ClassParameter.from(String.class, "packageName"),
                        ReflectionHelpers.ClassParameter.from(int.class, resid),
                        ReflectionHelpers.ClassParameter.from(ApplicationInfo.class, appInfo));
            }
        }
    }
}
