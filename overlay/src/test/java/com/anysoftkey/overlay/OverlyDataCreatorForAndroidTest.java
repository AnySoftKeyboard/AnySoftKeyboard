package com.anysoftkey.overlay;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowTypedArray;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class OverlyDataCreatorForAndroidTest {

    private OverlyDataCreatorForAndroid mUnderTest;
    private ComponentName mComponentName;
    private Context mRemoteContext;
    private Context mLocalContext;
    private PackageManager mLocalPackageManager;

    @Before
    public void setup() throws Exception {
        mLocalContext = Mockito.mock(Context.class);
        mUnderTest = new OverlyDataCreatorForAndroid(mLocalContext);
        mComponentName = new ComponentName("com.example", "com.example.Activity");

        mLocalPackageManager = Mockito.mock(PackageManager.class);
        ActivityInfo activityInfo = Mockito.mock(ActivityInfo.class);
        Mockito.doReturn(12).when(activityInfo).getThemeResource();
        Mockito.doReturn(activityInfo).when(mLocalPackageManager).getActivityInfo(mComponentName, PackageManager.GET_META_DATA);

        Mockito.doReturn(mLocalPackageManager).when(mLocalContext).getPackageManager();

        mRemoteContext = Mockito.mock(Context.class);
        Mockito.doReturn(mComponentName.getPackageName()).when(mRemoteContext).getPackageName();

        Mockito.doReturn(mRemoteContext).when(mLocalContext).createPackageContext(mComponentName.getPackageName(), CONTEXT_IGNORE_SECURITY);

        Resources remoteResources = Mockito.mock(Resources.class);
        Mockito.doReturn(4).when(remoteResources).getColor(3);
        Mockito.doReturn(6).when(remoteResources).getColor(5);
        Mockito.doReturn(8).when(remoteResources).getColor(7);
        Mockito.doReturn(remoteResources).when(mRemoteContext).getResources();

        setupReturnedColors(new int[]{Color.WHITE, Color.GRAY, Color.BLACK}, null);
    }

    private void setupReturnedColors(int[] colors, int[] references) {
        int[] data;
        if (colors != null) {
            data = new int[colors.length * 6];
            for (int colorIndex = 0; colorIndex < colors.length; colorIndex++) {
                final int offset = colorIndex * 6;
                data[offset] = TypedValue.TYPE_INT_COLOR_ARGB8;
                data[offset + 1 /*STYLE_DATA*/] = colors[colorIndex];
            }
        } else {
            data = new int[references.length * 6];
            for (int referenceIndex = 0; referenceIndex < references.length; referenceIndex++) {
                final int offset = referenceIndex * 6;
                data[offset] = TypedValue.TYPE_REFERENCE;
                data[offset + 3/*STYLE_RESOURCE_ID = 3*/] = references[referenceIndex];
            }
        }
        TypedArray array = ShadowTypedArray.create(RuntimeEnvironment.systemContext.getResources(),
                OverlyDataCreatorForAndroid.APP_COLORS_ATTRS,
                data, new int[]{0, 1, 2}, 3, new String[]{});

        Mockito.doReturn(array)
                .when(mRemoteContext)
                .obtainStyledAttributes(Mockito.eq(12), Mockito.same(OverlyDataCreatorForAndroid.APP_COLORS_ATTRS));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.KITKAT)
    public void testAlwaysInvalidWhenPriorToLollipop() {
        setupReturnedColors(new int[]{0, 1, 2}, null);
        Assert.assertFalse(mUnderTest.createOverlayData(mComponentName).isValid());
    }

    @Test
    public void testGetRawColorsHappyPath() throws Exception {
        setupReturnedColors(new int[]{0, 1, 2}, null);
        final OverlayData overlayData = mUnderTest.createOverlayData(mComponentName);

        Mockito.verify(mRemoteContext).obtainStyledAttributes(Mockito.eq(12), Mockito.same(OverlyDataCreatorForAndroid.APP_COLORS_ATTRS));
        final Resources resources = mRemoteContext.getResources();
        Mockito.verify(resources, Mockito.never()).getColor(Mockito.anyInt());

        Assert.assertEquals(0, overlayData.getPrimaryColor());
        Assert.assertEquals(1, overlayData.getPrimaryDarkColor());
        Assert.assertEquals(2, overlayData.getPrimaryTextColor());
        Assert.assertTrue(overlayData.isValid());
    }

    @Test
    public void testGetReferenceColorsHappyPath() {
        setupReturnedColors(null, new int[]{3, 5, 7});
        final OverlayData overlayData = mUnderTest.createOverlayData(mComponentName);

        final Resources resources = mRemoteContext.getResources();
        Mockito.verify(resources).getColor(3);
        Mockito.verify(resources).getColor(5);
        Mockito.verify(resources).getColor(7);

        Assert.assertEquals(4, overlayData.getPrimaryColor());
        Assert.assertEquals(6, overlayData.getPrimaryDarkColor());
        Assert.assertEquals(8, overlayData.getPrimaryTextColor());
        Assert.assertTrue(overlayData.isValid());
    }

    @Test
    public void testReturnsInvalidIfException() {
        final TypedArray typedArray = Mockito.mock(TypedArray.class);

        Mockito.doReturn(typedArray)
                .when(mRemoteContext)
                .obtainStyledAttributes(Mockito.eq(12), Mockito.same(OverlyDataCreatorForAndroid.APP_COLORS_ATTRS));

        Mockito.doThrow(new IllegalStateException()).when(typedArray).peekValue(Mockito.anyInt());

        Assert.assertFalse(mUnderTest.createOverlayData(mComponentName).isValid());
    }

    @Test
    public void testReturnsInvalidIfAppNotFound() throws Exception {
        setupReturnedColors(new int[]{0, 1, 2}, null);
        Mockito.doThrow(new PackageManager.NameNotFoundException()).when(mLocalPackageManager).getActivityInfo(Mockito.any(), Mockito.anyInt());

        Assert.assertFalse(mUnderTest.createOverlayData(mComponentName).isValid());
    }
}