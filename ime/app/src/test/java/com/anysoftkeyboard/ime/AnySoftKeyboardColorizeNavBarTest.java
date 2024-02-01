package com.anysoftkeyboard.ime;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowPhoneWindow;
import org.robolectric.shadows.ShadowResources;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(shadows = AnySoftKeyboardColorizeNavBarTest.TestShadowResources.class)
public class AnySoftKeyboardColorizeNavBarTest extends AnySoftKeyboardBaseTest {

  private int mMinimumHeight;

  @Before
  public void setUp() {
    mMinimumHeight =
        ApplicationProvider.getApplicationContext()
            .getResources()
            .getDimensionPixelOffset(R.dimen.navigation_bar_min_height);
  }

  @Test
  public void testHappyPath() {
    // addView+onStartView
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), times(2))
        .setBottomOffset(0);

    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

    simulateFinishInputFlow();
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    simulateOnStartInputFlow();

    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
        .setBottomOffset(TestShadowResources.NAVIGATION_BAR_HEIGHT);

    final Window w = mAnySoftKeyboardUnderTest.getWindow().getWindow();
    Assert.assertNotNull(w);
    Assert.assertTrue(
        Shadows.shadowOf(w).getFlag(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS));
    Assert.assertEquals(Color.TRANSPARENT, w.getNavigationBarColor());
  }

  @Test
  public void testExtraPadding() {
    simulateFinishInputFlow();
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
    simulateOnStartInputFlow();

    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
        .setBottomOffset(TestShadowResources.NAVIGATION_BAR_HEIGHT);

    simulateFinishInputFlow();
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_bottom_extra_padding_in_portrait, 6);
    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
    simulateOnStartInputFlow();

    // still shows the TestShadowResources.NAVIGATION_BAR_HEIGHT since it is higher padding
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
        .setBottomOffset(TestShadowResources.NAVIGATION_BAR_HEIGHT);

    simulateFinishInputFlow();
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_bottom_extra_padding_in_portrait, 12);
    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
    simulateOnStartInputFlow();

    // now uses the override since it is higher than TestShadowResources.NAVIGATION_BAR_HEIGHT
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
        .setBottomOffset(40 /*the minimum size*/ + 12);
  }

  @Test
  @Config(shadows = AnySoftKeyboardColorizeNavBarTest.TestShadowResourcesSmallHeight.class)
  public void testHappyPathForSmallNavigationBar() {
    // addView+onStartView
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), times(2))
        .setBottomOffset(0);

    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

    simulateFinishInputFlow();
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    simulateOnStartInputFlow();

    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
        .setBottomOffset(mMinimumHeight);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.R, shadows = TestShadowPhoneWindow.class)
  public void testHappyPathSdk30() {
    // addView+onStartView
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView()).setBottomOffset(0);
    simulateFinishInputFlow();
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
        .setBottomOffset(48 /*starts as enabled!*/);
    simulateFinishInputFlow();
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, false);
    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
    simulateOnStartInputFlow();
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView()).setBottomOffset(0);

    simulateFinishInputFlow();
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    simulateOnStartInputFlow();

    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
        .setBottomOffset(TestShadowResources.NAVIGATION_BAR_HEIGHT);

    Window w = mAnySoftKeyboardUnderTest.getWindow().getWindow();
    Assert.assertNotNull(w);
    TestShadowPhoneWindow shadowWindow = (TestShadowPhoneWindow) Shadows.shadowOf(w);
    Assert.assertTrue(shadowWindow.getFlag(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS));
    Assert.assertFalse(shadowWindow.decorFitsSystemWindows);
    Assert.assertEquals(Color.TRANSPARENT, w.getNavigationBarColor());
  }

  @Test
  public void testDoesNotClearPaddingIfRestartingInput() {
    // addView+onStartView
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), times(2))
        .setBottomOffset(0);

    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

    simulateFinishInputFlow();
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    simulateOnStartInputFlow();
    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
    simulateOnStartInputFlow(true, createEditorInfoTextWithSuggestionsForSetUp());

    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
        .setBottomOffset(TestShadowResources.NAVIGATION_BAR_HEIGHT);
    // ensuring setting padding was not called because of re-starting
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never())
        .setBottomOffset(0);
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
    // nothing happens in onFinish
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), never())
        .setBottomOffset(Mockito.anyInt());
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    simulateOnStartInputFlow();

    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
        .setBottomOffset(TestShadowResources.NAVIGATION_BAR_HEIGHT);

    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
    simulateFinishInputFlow();
    // not being reset
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), never())
        .setBottomOffset(0);
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, false);
    simulateOnStartInputFlow();
    Window w = mAnySoftKeyboardUnderTest.getWindow().getWindow();
    Assert.assertNotNull(w);
    Assert.assertFalse(
        Shadows.shadowOf(w).getFlag(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS));

    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
    simulateFinishInputFlow();
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    simulateOnStartInputFlow();
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView())
        .setBottomOffset(TestShadowResources.NAVIGATION_BAR_HEIGHT);
    w = mAnySoftKeyboardUnderTest.getWindow().getWindow();
    Assert.assertNotNull(w);
    Assert.assertTrue(
        Shadows.shadowOf(w).getFlag(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS));
    Assert.assertEquals(Color.TRANSPARENT, w.getNavigationBarColor());
  }

  @Test
  @Config(shadows = TestShadowResources.class, sdk = Build.VERSION_CODES.KITKAT)
  public void testDoesNotSetPaddingBeforeLollipop() throws Exception {
    // addView
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView()).setBottomOffset(0);

    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

    simulateFinishInputFlow();
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    simulateOnStartInputFlow();

    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), Mockito.never())
        .setBottomOffset(Mockito.anyInt());
  }

  @Test
  @Config(shadows = {TestShadowResources.class, TestShadowResourcesFalseConfig.class})
  public void testDoesNotSetPaddingIfOsSaysNoNavBar() throws Exception {
    // was set as zero padding addView+onStartView
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), times(2))
        .setBottomOffset(0);

    simulateFinishInputFlow();
    // nothing changed
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), times(2))
        .setBottomOffset(Mockito.anyInt());

    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    simulateOnStartInputFlow();
    // now, again, set to zero
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), times(3))
        .setBottomOffset(0);
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), times(3))
        .setBottomOffset(Mockito.anyInt());
  }

  @Test
  @Config(shadows = {TestShadowResources.class, TestShadowResourcesNoConfigResId.class})
  public void testDoesNotSetPaddingIfNoConfigResource() throws Exception {
    // addView+onStartView
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), times(2))
        .setBottomOffset(0);
    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
    simulateFinishInputFlow();
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), never())
        .setBottomOffset(Mockito.anyInt());
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    simulateOnStartInputFlow();
    // set to zero
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView()).setBottomOffset(0);
  }

  @Test
  @Config(shadows = TestShadowResources.class, qualifiers = "w420dp-h640dp-land-mdpi")
  public void testSetsPaddingInLandscape() throws Exception {
    // was set to zero padding in the addView+onStart in the setup method
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), times(2))
        .setBottomOffset(0);
    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
    simulateFinishInputFlow();
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), never())
        .setBottomOffset(Mockito.anyInt());
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    simulateOnStartInputFlow();
    // sets to needed padding
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView()).setBottomOffset(48);
  }

  @Test
  @Config(shadows = TestShadowResourcesNoResId.class)
  public void testDoesNotSetPaddingIfNoNavigationBarRes() throws Exception {
    // the initial add+onStartView
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), times(2))
        .setBottomOffset(0);
    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

    simulateFinishInputFlow();
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    simulateOnStartInputFlow();

    // sets to zero again
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), times(1))
        .setBottomOffset(0);
  }

  @Test
  @Config(shadows = TestShadowResourcesZeroHeight.class)
  public void testDoesNotSetPaddingIfNavHeightIsZero() throws Exception {
    Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
    simulateFinishInputFlow();
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), never())
        .setBottomOffset(Mockito.anyInt());

    SharedPrefsHelper.setPrefsValue(R.string.settings_key_colorize_nav_bar, true);
    simulateOnStartInputFlow();
    // since size is zero, we hide the padding (set to zero)
    Mockito.verify((AnyKeyboardView) mAnySoftKeyboardUnderTest.getInputView(), times(1))
        .setBottomOffset(0);
  }

  @Implements(Resources.class)
  public static class TestShadowResources extends ShadowResources {
    static int RES_ID = 18263213;
    static int RES_CONFIG_ID = 19263224;
    static int NAVIGATION_BAR_HEIGHT = 48;

    @RealObject Resources mResources;

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
        return Shadow.directlyOn(
            mResources,
            Resources.class,
            "getIdentifier",
            ClassParameter.from(String.class, name),
            ClassParameter.from(String.class, defType),
            ClassParameter.from(String.class, defPackage));
      }
    }

    @Implementation
    protected int getDimensionPixelSize(int id) throws Resources.NotFoundException {
      if (id == RES_ID) {
        return NAVIGATION_BAR_HEIGHT;
      } else {
        return Shadow.directlyOn(
            mResources,
            Resources.class,
            "getDimensionPixelSize",
            ClassParameter.from(int.class, id));
      }
    }

    @Implementation
    protected boolean getBoolean(int id) throws Resources.NotFoundException {
      if (id == RES_CONFIG_ID) {
        return true;
      } else {
        return Shadow.directlyOn(
            mResources, Resources.class, "getBoolean", ClassParameter.from(int.class, id));
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
  public static class TestShadowResourcesSmallHeight extends TestShadowResources {

    static int NAVIGATION_BAR_2_HEIGHT = 16;

    @Implementation
    @Override
    protected int getDimensionPixelSize(int id) throws Resources.NotFoundException {
      if (id == RES_ID) {
        return NAVIGATION_BAR_2_HEIGHT;
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
        return Shadow.directlyOn(
            mResources, Resources.class, "getBoolean", ClassParameter.from(int.class, id));
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

  @Implements(
      className = "com.android.internal.policy.PhoneWindow",
      isInAndroidSdk = false,
      minSdk = Build.VERSION_CODES.R,
      looseSignatures = true)
  public static class TestShadowPhoneWindow extends ShadowPhoneWindow {
    Boolean decorFitsSystemWindows = null;
    @RealObject Window mWindows;

    @Implementation
    public void setDecorFitsSystemWindows(boolean decorFitsSystemWindows) {
      this.decorFitsSystemWindows = decorFitsSystemWindows;
      directlyOn(
          mWindows,
          Window.class,
          "setDecorFitsSystemWindows",
          ReflectionHelpers.ClassParameter.from(boolean.class, decorFitsSystemWindows));
    }
  }
}
