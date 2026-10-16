package com.anysoftkeyboard.ime;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Dialog;
import android.os.Build;
import android.view.LayoutInflater;
import android.window.BackEvent;
import android.window.OnBackAnimationCallback;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.menny.android.anysoftkeyboard.R;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class MainKeyboardBackAnimationHelperTest {

  @Test
  @Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testPredictiveBackAnimationLifecycleOnApi34() {
    final Dialog dialog = new Dialog(getApplicationContext());
    dialog.show();

    final KeyboardViewContainerView container =
        (KeyboardViewContainerView)
            LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.main_keyboard_layout, null, false);
    container.layout(0, 0, 1000, 500);

    final AtomicBoolean hideWindowCalled = new AtomicBoolean(false);
    final AtomicBoolean animationsEnabled = new AtomicBoolean(true);

    final MainKeyboardBackAnimationHelper helper =
        MainKeyboardBackAnimationHelper.create(
            dialog, () -> container, () -> hideWindowCalled.set(true), animationsEnabled::get);

    Assert.assertNotNull(helper);
    helper.register();

    final OnBackAnimationCallback callback = (OnBackAnimationCallback) helper.getCallback();
    Assert.assertNotNull(callback);

    callback.onBackStarted(new BackEvent(10f, 10f, 0.0f, BackEvent.EDGE_LEFT));
    Assert.assertEquals(0f, container.getTranslationY(), 0.01f);
    Assert.assertEquals(1.0f, container.getAlpha(), 0.01f);
    Assert.assertEquals(0f, container.getPredictiveBackProgress(), 0.01f);

    callback.onBackProgressed(new BackEvent(50f, 10f, 0.5f, BackEvent.EDGE_LEFT));
    Assert.assertEquals(0.5f, container.getPredictiveBackProgress(), 0.01f);
    Assert.assertEquals(75f, container.getTranslationY(), 0.01f);
    Assert.assertEquals(0.8f, container.getAlpha(), 0.01f);

    callback.onBackProgressed(new BackEvent(80f, 10f, 0.8f, BackEvent.EDGE_LEFT));
    Assert.assertEquals(0.8f, container.getPredictiveBackProgress(), 0.01f);
    Assert.assertEquals(120f, container.getTranslationY(), 0.01f);
    Assert.assertEquals(0.68f, container.getAlpha(), 0.01f);

    callback.onBackCancelled();
    container.resetPredictiveBackProgress();
    Assert.assertEquals(0f, container.getTranslationY(), 0.01f);
    Assert.assertEquals(1.0f, container.getAlpha(), 0.01f);
    Assert.assertEquals(0f, container.getPredictiveBackProgress(), 0.01f);

    callback.onBackInvoked();
    Assert.assertTrue(hideWindowCalled.get());

    helper.unregister();
    dialog.dismiss();
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testAnimationsDisabledGating() {
    final Dialog dialog = new Dialog(getApplicationContext());
    dialog.show();

    final KeyboardViewContainerView container =
        (KeyboardViewContainerView)
            LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.main_keyboard_layout, null, false);
    container.layout(0, 0, 1000, 500);

    final AtomicBoolean hideWindowCalled = new AtomicBoolean(false);
    final AtomicBoolean animationsEnabled = new AtomicBoolean(false);

    final MainKeyboardBackAnimationHelper helper =
        MainKeyboardBackAnimationHelper.create(
            dialog, () -> container, () -> hideWindowCalled.set(true), animationsEnabled::get);

    Assert.assertNotNull(helper);
    final OnBackAnimationCallback callback = (OnBackAnimationCallback) helper.getCallback();
    Assert.assertNotNull(callback);

    callback.onBackProgressed(new BackEvent(50f, 10f, 0.5f, BackEvent.EDGE_LEFT));
    Assert.assertEquals(0f, container.getPredictiveBackProgress(), 0.01f);
    Assert.assertEquals(0f, container.getTranslationY(), 0.01f);
    Assert.assertEquals(1.0f, container.getAlpha(), 0.01f);

    dialog.dismiss();
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.TIRAMISU)
  public void testNullOnApi33() {
    final Dialog dialog = new Dialog(getApplicationContext());
    dialog.show();

    final MainKeyboardBackAnimationHelper helper =
        MainKeyboardBackAnimationHelper.create(dialog, () -> null, () -> {}, () -> true);

    Assert.assertNull(helper);
    dialog.dismiss();
  }
}
