package com.anysoftkeyboard.ime;

import android.content.res.Resources;
import android.os.Build;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowKeyCharacterMap;
import org.robolectric.shadows.ShadowResources;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(shadows = AnySoftKeyboardColorizeNavBarTest.TestShadowResources.class)
public class AnySoftKeyboardColorizeNavBarTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testHappyPath() {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView()).setBottomOffset(32);
    }

    @Test
    @Config(shadows = TestShadowResources.class, sdk = Build.VERSION_CODES.KITKAT)
    public void testDoesNotSetPaddingBeforeLollipop() throws Exception {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setBottomOffset(Mockito.anyInt());
    }

    @Test
    @Config(shadows = {TestShadowResources.class, MyShadowKeyCharacterMapWithBack.class})
    public void testDoesNotSetPaddingIfHasBackKey() throws Exception {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setBottomOffset(Mockito.anyInt());
    }

    @Test
    @Config(shadows = {TestShadowResources.class, MyShadowKeyCharacterMapWithHome.class})
    public void testDoesNotSetPaddingIfHasHomeKey() throws Exception {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setBottomOffset(Mockito.anyInt());
    }

    @Test
    @Config(shadows = TestShadowResources.class, qualifiers = "w420dp-h640dp-land-mdpi")
    public void testDoesNotSetPaddingInLandscape() throws Exception {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setBottomOffset(Mockito.anyInt());
    }

    @Test
    @Config(shadows = TestShadowResourcesNoResId.class)
    public void testDoesNotSetPaddingIfNoNavigationBarRes() throws Exception {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setBottomOffset(Mockito.anyInt());
    }

    @Test
    @Config(shadows = TestShadowResourcesZeroHeight.class)
    public void testDoesNotSetPaddingIfNavHeightIsZero() throws Exception {
        Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setBottomOffset(Mockito.anyInt());
    }

    @Implements(Resources.class)
    public static class TestShadowResources extends ShadowResources {
        @RealObject
        Resources mResources;

        static int RES_ID = 18263213;

        @Implementation
        protected int getIdentifier(String name, String defType, String defPackage) {
            if ("navigation_bar_height".equals(name) && "dimen".equals(defType) && "android".equals(defPackage)) {
                return RES_ID;
            } else {
                return Shadow.directlyOn(mResources, Resources.class).getIdentifier(name, defType, defPackage);
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
    public static class TestShadowResourcesNoResId extends TestShadowResources {

        @Implementation
        @Override
        protected int getIdentifier(String name, String defType, String defPackage) {
            if ("navigation_bar_height".equals(name) && "dimen".equals(defType) && "android".equals(defPackage)) {
                return 0;
            } else {
                return super.getIdentifier(name, defType, defPackage);
            }
        }
    }


    @Implements(KeyCharacterMap.class)
    public static class MyShadowKeyCharacterMapWithHome extends ShadowKeyCharacterMap {
        @Implementation
        protected static boolean deviceHasKey(int keyCode) {
            return keyCode == KeyEvent.KEYCODE_HOME;
        }
    }

    @Implements(KeyCharacterMap.class)
    public static class MyShadowKeyCharacterMapWithBack extends ShadowKeyCharacterMap {
        @Implementation
        protected static boolean deviceHasKey(int keyCode) {
            return keyCode == KeyEvent.KEYCODE_BACK;
        }
    }
}