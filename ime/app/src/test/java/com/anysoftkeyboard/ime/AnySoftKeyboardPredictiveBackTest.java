package com.anysoftkeyboard.ime;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.provider.Settings;
import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardPredictiveBackTest extends AnySoftKeyboardBaseTest {

  @Test
  public void testNullHelperOnPreApi34() {
    Assert.assertNull(mAnySoftKeyboardUnderTest.getDismissAnimationHelper());
    Assert.assertNull(mAnySoftKeyboardUnderTest.createDismissAnimationHelper());
  }

  @Test
  public void testResetOnFinishInputViewAndWindowHidden() {
    final KeyboardViewContainerView container = mAnySoftKeyboardUnderTest.getInputViewContainer();
    Assert.assertNotNull(container);
    container.layout(0, 0, 1000, 500);
    container.setPredictiveBackProgress(0.5f);

    mAnySoftKeyboardUnderTest.onWindowHidden();
    Assert.assertEquals(0f, container.getTranslationY(), 0.01f);
    Assert.assertEquals(1.0f, container.getAlpha(), 0.01f);
    Assert.assertEquals(0f, container.getPredictiveBackProgress(), 0.01f);
  }

  @Test
  public void testResetOnHideWindow() {
    final KeyboardViewContainerView container = mAnySoftKeyboardUnderTest.getInputViewContainer();
    Assert.assertNotNull(container);
    container.layout(0, 0, 1000, 500);
    container.setPredictiveBackProgress(0.7f);

    mAnySoftKeyboardUnderTest.hideWindow();
    Assert.assertEquals(0f, container.getTranslationY(), 0.01f);
    Assert.assertEquals(1.0f, container.getAlpha(), 0.01f);
    Assert.assertEquals(0f, container.getPredictiveBackProgress(), 0.01f);
  }

  @Test
  public void testAreAnimationsEnabledDefault() {
    Settings.Global.putFloat(
        getApplicationContext().getContentResolver(),
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1.0f);
    Settings.Global.putFloat(
        getApplicationContext().getContentResolver(),
        Settings.Global.TRANSITION_ANIMATION_SCALE,
        1.0f);
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_tweak_animations_level, "some");
    Assert.assertTrue(mAnySoftKeyboardUnderTest.areAnimationsEnabled());
  }

  @Test
  public void testAreAnimationsEnabledDisabledViaAnimatorDuration() {
    Settings.Global.putFloat(
        getApplicationContext().getContentResolver(),
        Settings.Global.ANIMATOR_DURATION_SCALE,
        0.0f);
    Settings.Global.putFloat(
        getApplicationContext().getContentResolver(),
        Settings.Global.TRANSITION_ANIMATION_SCALE,
        1.0f);
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_tweak_animations_level, "some");
    Assert.assertFalse(mAnySoftKeyboardUnderTest.areAnimationsEnabled());
  }

  @Test
  public void testAreAnimationsEnabledDisabledViaTransitionAnimation() {
    Settings.Global.putFloat(
        getApplicationContext().getContentResolver(),
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1.0f);
    Settings.Global.putFloat(
        getApplicationContext().getContentResolver(),
        Settings.Global.TRANSITION_ANIMATION_SCALE,
        0.0f);
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_tweak_animations_level, "some");
    Assert.assertFalse(mAnySoftKeyboardUnderTest.areAnimationsEnabled());
  }

  @Test
  public void testAreAnimationsEnabledDisabledViaPref() {
    Settings.Global.putFloat(
        getApplicationContext().getContentResolver(),
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1.0f);
    Settings.Global.putFloat(
        getApplicationContext().getContentResolver(),
        Settings.Global.TRANSITION_ANIMATION_SCALE,
        1.0f);
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_tweak_animations_level, "none");
    Assert.assertFalse(mAnySoftKeyboardUnderTest.areAnimationsEnabled());
  }

  @Test
  public void testDismissAnimationHelperLifecycleWithMock() {
    final MainKeyboardBackAnimationHelper mockHelper =
        Mockito.mock(MainKeyboardBackAnimationHelper.class);
    mAnySoftKeyboardUnderTest.setDismissAnimationHelperFactory(() -> mockHelper);

    // start input view -> helper registered
    mAnySoftKeyboardUnderTest.onStartInputView(new EditorInfo(), false);
    Assert.assertSame(mockHelper, mAnySoftKeyboardUnderTest.getDismissAnimationHelper());
    Mockito.verify(mockHelper).register();

    // dialog opens -> hasStuffToClose is true -> mockHelper unregistered and nulled out
    mAnySoftKeyboardUnderTest.onDialogStateChanged(true);
    Assert.assertNull(mAnySoftKeyboardUnderTest.getDismissAnimationHelper());
    Mockito.verify(mockHelper).unregister();

    // dialog closes -> hasStuffToClose is false -> helper recreated and registered
    Mockito.clearInvocations(mockHelper);
    mAnySoftKeyboardUnderTest.onDialogStateChanged(false);
    Assert.assertSame(mockHelper, mAnySoftKeyboardUnderTest.getDismissAnimationHelper());
    Mockito.verify(mockHelper).register();

    // input view finishes -> unregistered and nulled out
    mAnySoftKeyboardUnderTest.onFinishInputView(false);
    Assert.assertNull(mAnySoftKeyboardUnderTest.getDismissAnimationHelper());
    Mockito.verify(mockHelper).unregister();

    // start input view again
    Mockito.clearInvocations(mockHelper);
    mAnySoftKeyboardUnderTest.onStartInputView(new EditorInfo(), false);
    Assert.assertSame(mockHelper, mAnySoftKeyboardUnderTest.getDismissAnimationHelper());
    Mockito.verify(mockHelper).register();

    // window hidden -> unregistered and nulled out
    mAnySoftKeyboardUnderTest.onWindowHidden();
    Assert.assertNull(mAnySoftKeyboardUnderTest.getDismissAnimationHelper());
    Mockito.verify(mockHelper).unregister();

    // start input view again
    Mockito.clearInvocations(mockHelper);
    mAnySoftKeyboardUnderTest.onStartInputView(new EditorInfo(), false);
    Assert.assertSame(mockHelper, mAnySoftKeyboardUnderTest.getDismissAnimationHelper());
    Mockito.verify(mockHelper).register();

    // onDestroy -> unregisters and clears
    mAnySoftKeyboardUnderTest.onDestroy();
    Assert.assertNull(mAnySoftKeyboardUnderTest.getDismissAnimationHelper());
    Mockito.verify(mockHelper).unregister();
  }
}
