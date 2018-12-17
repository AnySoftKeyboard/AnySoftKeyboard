package com.anysoftkey.overlay;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Build;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.overlay.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPackageManager;

import androidx.annotation.StyleRes;
import androidx.test.core.app.ApplicationProvider;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class OverlyDataCreatorForAndroidTest {

    private OverlyDataCreatorForAndroid mUnderTest;
    private ComponentName mComponentName;

    @Before
    public void setup() throws Exception {
        mComponentName = new ComponentName("com.example", "com.example.Activity");
        final Context applicationContext = Mockito.spy(ApplicationProvider.getApplicationContext());
        Mockito.doReturn(applicationContext).when(applicationContext).createPackageContext(mComponentName.getPackageName(), CONTEXT_IGNORE_SECURITY);
        mUnderTest = new OverlyDataCreatorForAndroid(applicationContext);
    }

    private void setupReturnedColors(@StyleRes int theme) {
        final ShadowPackageManager shadowPackageManager = Shadows.shadowOf(ApplicationProvider.getApplicationContext().getPackageManager());
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.packageName = mComponentName.getPackageName();
        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.packageName = packageInfo.packageName;
        activityInfo.theme = theme;
        activityInfo.name = mComponentName.getClassName();

        packageInfo.activities = new ActivityInfo[]{activityInfo};

        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.theme = theme;
        applicationInfo.packageName = mComponentName.getPackageName();

        shadowPackageManager.addPackage(packageInfo);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.KITKAT)
    public void testAlwaysInvalidWhenPriorToLollipop() {
        setupReturnedColors(R.style.HappyPathRawColors);
        Assert.assertFalse(mUnderTest.createOverlayData(mComponentName).isValid());
    }

    @Test
    public void testGetRawColorsHappyPath() throws Exception {
        setupReturnedColors(R.style.HappyPathRawColors);
        final OverlayData overlayData = mUnderTest.createOverlayData(mComponentName);

        Assert.assertEquals(Color.parseColor("#ffcc9900"), overlayData.getPrimaryColor());
        Assert.assertEquals(Color.parseColor("#aacc9900"), overlayData.getPrimaryDarkColor());
        Assert.assertEquals(Color.parseColor("#ff0099cc"), overlayData.getPrimaryTextColor());
        Assert.assertTrue(overlayData.isValid());
    }

    @Test
    public void testGetReferenceColorsHappyPath() {
        setupReturnedColors(R.style.HappyPathReferenceColors);
        final OverlayData overlayData = mUnderTest.createOverlayData(mComponentName);

        Assert.assertEquals(Color.parseColor("#ffcc9900"), overlayData.getPrimaryColor());
        Assert.assertEquals(Color.parseColor("#aacc9900"), overlayData.getPrimaryDarkColor());
        Assert.assertEquals(Color.parseColor("#ffff0000"), overlayData.getPrimaryTextColor());
        Assert.assertTrue(overlayData.isValid());
    }

    @Test
    public void testDoesNotFailIfMissingAttributeInTheme() {
        setupReturnedColors(R.style.MissingAttribute);
        final OverlayData overlayData = mUnderTest.createOverlayData(mComponentName);

        //primary and dark-primary are the defaults of the OS/SDK-level. I don't want to
        //verify their values since it may change.
        Assert.assertEquals(Color.parseColor("#ffff0000"), overlayData.getPrimaryTextColor());
        Assert.assertTrue(overlayData.isValid());
    }

    @Test
    public void testReturnDarkAsPrimaryIfMissing() {
        setupReturnedColors(R.style.MissingDarkAttribute);
        final OverlayData overlayData = mUnderTest.createOverlayData(mComponentName);
        Assert.assertTrue(overlayData.isValid());

        Assert.assertEquals(Color.parseColor("#ffcc9900"), overlayData.getPrimaryColor());
        Assert.assertEquals(Color.parseColor("#ffcc9900"), overlayData.getPrimaryDarkColor());
        Assert.assertEquals(Color.parseColor("#ffff0000"), overlayData.getPrimaryTextColor());
    }

    @Test
    public void testReturnsInvalidIfAppNotFound() throws Exception {
        Assert.assertFalse(mUnderTest.createOverlayData(new ComponentName("com.not.here", "Activity")).isValid());
    }
}