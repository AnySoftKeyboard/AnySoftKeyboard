package com.anysoftkeyboard.overlay;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;

import androidx.test.core.app.ApplicationProvider;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ThemeOverlayCombinerTest {

    @Test
    public void testProvidesColorDrawableIfThemeDidNotProvideDrawables() {
        ThemeOverlayCombiner combiner = new ThemeOverlayCombiner();

        OverlayData data = new OverlayData();
        data.setPrimaryColor(1);
        data.setPrimaryDarkColor(2);
        data.setPrimaryTextColor(3);
        Assert.assertTrue(data.isValid());

        combiner.setOverlayData(data);

        ThemeResourcesHolder themeResources = combiner.getThemeResources();
        Assert.assertEquals(1, ((ColorDrawable) themeResources.getKeyBackground()).getColor());
        Assert.assertEquals(2, ((ColorDrawable) themeResources.getKeyboardBackground()).getColor());
    }

    @Test
    public void testHappyPath() {
        Resources resources = ApplicationProvider.getApplicationContext().getResources();
        ThemeOverlayCombiner combiner = new ThemeOverlayCombiner();

        combiner.setThemeKeyBackground(resources.getDrawable(R.drawable.test_image_1));
        combiner.setThemeKeyboardBackground(resources.getDrawable(R.drawable.test_image_2));

        OverlayData data = new OverlayData();
        data.setPrimaryColor(1);
        data.setPrimaryDarkColor(2);
        data.setPrimaryTextColor(3);
        Assert.assertTrue(data.isValid());

        combiner.setOverlayData(data);

        ThemeResourcesHolder themeResources = combiner.getThemeResources();
        Assert.assertEquals(1, extractColorFromFilter(themeResources.getKeyBackground()));
        Assert.assertEquals(2, extractColorFromFilter(themeResources.getKeyboardBackground()));
        Assert.assertEquals(3, themeResources.getKeyTextColor().getDefaultColor());
        Assert.assertEquals(3, themeResources.getNameTextColor());

        combiner.setThemeKeyBackground(resources.getDrawable(R.drawable.test_image_3));
        themeResources = combiner.getThemeResources();
        Assert.assertEquals(1, extractColorFromFilter(themeResources.getKeyBackground()));
        Assert.assertEquals(2, extractColorFromFilter(themeResources.getKeyboardBackground()));
        Assert.assertEquals(3, themeResources.getKeyTextColor().getDefaultColor());
        Assert.assertEquals(3, themeResources.getNameTextColor());

        data.setPrimaryColor(4);
        data.setPrimaryDarkColor(5);
        data.setPrimaryTextColor(6);
        Assert.assertTrue(data.isValid());
        combiner.setOverlayData(data);
        Assert.assertEquals(4, extractColorFromFilter(themeResources.getKeyBackground()));
        Assert.assertEquals(5, extractColorFromFilter(themeResources.getKeyboardBackground()));
        Assert.assertEquals(6, themeResources.getKeyTextColor().getDefaultColor());
        Assert.assertEquals(6, themeResources.getNameTextColor());

        //setting invalid value
        combiner.setOverlayData(new OverlayData());
        themeResources = combiner.getThemeResources();
        Assert.assertEquals(R.drawable.test_image_3, Shadows.shadowOf(themeResources.getKeyBackground()).getCreatedFromResId());
        Assert.assertNull(themeResources.getKeyBackground().getColorFilter());
        Assert.assertEquals(R.drawable.test_image_2, Shadows.shadowOf(themeResources.getKeyboardBackground()).getCreatedFromResId());
        Assert.assertNull(themeResources.getKeyboardBackground().getColorFilter());

        combiner.setThemeKeyboardBackground(resources.getDrawable(R.drawable.test_image_1));
        combiner.setThemeTextColor(new ColorStateList(new int[][]{{0}}, new int[]{Color.GRAY}));
        combiner.setThemeNameTextColor(Color.DKGRAY);
        combiner.setThemeHintTextColor(Color.BLUE);

        themeResources = combiner.getThemeResources();
        Assert.assertEquals(R.drawable.test_image_3, Shadows.shadowOf(themeResources.getKeyBackground()).getCreatedFromResId());
        Assert.assertNull(themeResources.getKeyBackground().getColorFilter());
        Assert.assertEquals(R.drawable.test_image_1, Shadows.shadowOf(themeResources.getKeyboardBackground()).getCreatedFromResId());
        Assert.assertNull(themeResources.getKeyboardBackground().getColorFilter());
        Assert.assertEquals(Color.GRAY, themeResources.getKeyTextColor().getDefaultColor());
        Assert.assertEquals(Color.DKGRAY, themeResources.getNameTextColor());
        Assert.assertEquals(Color.BLUE, themeResources.getHintTextColor());
    }

    private static int extractColorFromFilter(Drawable drawable) {
        return ((LightingColorFilter) drawable.getColorFilter()).getColorAdd();
    }
}