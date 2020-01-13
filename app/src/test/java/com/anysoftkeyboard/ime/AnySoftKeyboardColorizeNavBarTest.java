package com.anysoftkeyboard.ime;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowResources;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(shadows = AnySoftKeyboardColorizeNavBarTest.TestShadowResources.class)
public class AnySoftKeyboardColorizeNavBarTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testHappyPath() {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
                .setBottomOffset(0);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
        simulateOnStartInputFlow();

        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
                .setBottomOffset(32);

        final Window w = mAnySoftKeyboardUnderTest.getWindow().getWindow();
        Assert.assertNotNull(w);
        Assert.assertTrue(
                Shadows.shadowOf(w).getFlag(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS));
        Assert.assertEquals(Color.TRANSPARENT, w.getNavigationBarColor());
    }

    @Test
    public void testDoNotDrawIfSettingIsOff() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
        simulateOnStartInputFlow();

        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
                .setBottomOffset(32);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, false);
        simulateOnStartInputFlow();
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
                .setBottomOffset(0);
        Window w = mAnySoftKeyboardUnderTest.getWindow().getWindow();
        Assert.assertNotNull(w);
        Assert.assertFalse(
                Shadows.shadowOf(w).getFlag(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS));

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
        simulateOnStartInputFlow();
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
                .setBottomOffset(32);
        w = mAnySoftKeyboardUnderTest.getWindow().getWindow();
        Assert.assertNotNull(w);
        Assert.assertTrue(
                Shadows.shadowOf(w).getFlag(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS));
        Assert.assertEquals(Color.TRANSPARENT, w.getNavigationBarColor());
    }

    @Test
    @Config(shadows = TestShadowResources.class, sdk = Build.VERSION_CODES.KITKAT)
    public void testDoesNotSetPaddingBeforeLollipop() throws Exception {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never())
                .setBottomOffset(Mockito.anyInt());

        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
        simulateOnStartInputFlow();

        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never())
                .setBottomOffset(Mockito.anyInt());
    }

    @Test
    @Config(shadows = {TestShadowResources.class, TestShadowResourcesFalseConfig.class})
    public void testDoesNotSetPaddingIfOsSaysNoNavBar() throws Exception {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never())
                .setBottomOffset(Mockito.anyInt());

        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
        simulateOnStartInputFlow();

        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never())
                .setBottomOffset(Mockito.anyInt());
    }

    @Test
    @Config(shadows = {TestShadowResources.class, TestShadowResourcesNoConfigResId.class})
    public void testDoesNotSetPaddingIfNoConfigResource() throws Exception {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never())
                .setBottomOffset(Mockito.anyInt());
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
        simulateOnStartInputFlow();

        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never())
                .setBottomOffset(Mockito.anyInt());
    }

    @Test
    @Config(shadows = TestShadowResources.class, qualifiers = "w420dp-h640dp-land-mdpi")
    public void testDoesNotSetPaddingInLandscape() throws Exception {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never())
                .setBottomOffset(Mockito.anyInt());
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
        simulateOnStartInputFlow();

        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never())
                .setBottomOffset(Mockito.anyInt());
    }

    @Test
    @Config(shadows = TestShadowResourcesNoResId.class)
    public void testDoesNotSetPaddingIfNoNavigationBarRes() throws Exception {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
                .setBottomOffset(0);
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
        simulateOnStartInputFlow();

        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
                .setBottomOffset(0);
    }

    @Test
    @Config(shadows = TestShadowResourcesZeroHeight.class)
    public void testDoesNotSetPaddingIfNavHeightIsZero() throws Exception {
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
        simulateOnStartInputFlow();

        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
                .setBottomOffset(0);
    }

    @Implements(Resources.class)
    public static class TestShadowResources extends ShadowResources {
        @RealObject Resources mResources;

        static int RES_ID = 18263213;
        static int RES_CONFIG_ID = 19263224;

        @Implementation
        protected int getIdentifier(String name, String defType, String defPackage) {
            if ("navigation_bar_height".equals(name)
                    && "dimen".equals(defType)
                    && "android".equals(defPackage)) {
                return RES_ID;
            } else if ("config_showNavigationBar".equals(name)
                    && "bool".equals(defType)
                    && "android".equals(defPackage)) {
                return RES_CONFIG_ID;
            } else {
                return Shadow.directlyOn(mResources, Resources.class)
                        .getIdentifier(name, defType, defPackage);
            }
        }

        @Implementation
        protected int getDimensionPixelSize(int id) throws Resources.NotFoundException {
            if (id == RES_ID) {
                return 32;
            } else {
                return Shadow.directlyOn(mResources, Resources.class).getDimensionPixelSize(id);
            }
        }

        @Implementation
        protected boolean getBoolean(int id) throws Resources.NotFoundException {
            if (id == RES_CONFIG_ID) {
                return true;
            } else {
                return Shadow.directlyOn(mResources, Resources.class).getBoolean(id);
            }
        }
    }

    @Implements(Resources.class)
    public static class TestShadowResourcesZeroHeight extends TestShadowResources {

        @Implementation
        @Override
        protected int getDimensionPixelSize(int id) throws Resources.NotFoundException {
            if (id == RES_ID) {
                return 0;
            } else {
                return super.getDimensionPixelSize(id);
            }
        }
    }

    @Implements(Resources.class)
    public static class TestShadowResourcesFalseConfig extends TestShadowResources {

        @Implementation
        @Override
        protected boolean getBoolean(int id) throws Resources.NotFoundException {
            if (id == RES_CONFIG_ID) {
                return false;
            } else {
                return Shadow.directlyOn(mResources, Resources.class).getBoolean(id);
            }
        }
    }

    @Implements(Resources.class)
    public static class TestShadowResourcesNoConfigResId extends TestShadowResources {

        @Implementation
        @Override
        protected int getIdentifier(String name, String defType, String defPackage) {
            if ("config_showNavigationBar".equals(name)
                    && "bool".equals(defType)
                    && "android".equals(defPackage)) {
                return 0;
            } else {
                return super.getIdentifier(name, defType, defPackage);
            }
        }
    }

    @Implements(Resources.class)
    public static class TestShadowResourcesNoResId extends TestShadowResources {

        @Implementation
        @Override
        protected int getIdentifier(String name, String defType, String defPackage) {
            if ("navigation_bar_height".equals(name)
                    && "dimen".equals(defType)
                    && "android".equals(defPackage)) {
                return 0;
            } else {
                return super.getIdentifier(name, defType, defPackage);
            }
        }
    }
}
