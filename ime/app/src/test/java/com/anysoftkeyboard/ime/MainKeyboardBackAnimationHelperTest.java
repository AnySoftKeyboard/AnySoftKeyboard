/*
 * Copyright (c) 2026 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ime;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.Window;
import android.window.BackEvent;
import android.window.OnBackAnimationCallback;
import android.window.OnBackInvokedDispatcher;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.menny.android.anysoftkeyboard.R;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class MainKeyboardBackAnimationHelperTest {

  @Test
  @Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testPredictiveBackAnimationLifecycleOnApi34() {
    final Window window = Mockito.mock(Window.class);
    final OnBackInvokedDispatcher dispatcher = Mockito.mock(OnBackInvokedDispatcher.class);
    Mockito.doReturn(dispatcher).when(window).getOnBackInvokedDispatcher();

    final KeyboardViewContainerView container =
        (KeyboardViewContainerView)
            LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.main_keyboard_layout, null, false);
    container.layout(0, 0, 1000, 500);

    final AtomicBoolean hideWindowCalled = new AtomicBoolean(false);
    final AtomicBoolean animationsEnabled = new AtomicBoolean(true);

    final MainKeyboardBackAnimationHelper helper =
        MainKeyboardBackAnimationHelper.create(
            () -> window,
            () -> container,
            () -> hideWindowCalled.set(true),
            animationsEnabled::get);

    Assert.assertNotNull(helper);
    helper.register();
    helper.register(); // Idempotent check
    Mockito.verify(dispatcher, Mockito.times(1))
        .registerOnBackInvokedCallback(
            Mockito.eq(OnBackInvokedDispatcher.PRIORITY_DEFAULT),
            Mockito.any(OnBackAnimationCallback.class));

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
    helper.unregister(); // Idempotent check
    Mockito.verify(dispatcher, Mockito.times(1))
        .unregisterOnBackInvokedCallback(Mockito.eq(callback));
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testAnimationsDisabledGating() {
    final Window window = Mockito.mock(Window.class);
    final KeyboardViewContainerView container =
        (KeyboardViewContainerView)
            LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.main_keyboard_layout, null, false);
    container.layout(0, 0, 1000, 500);

    final AtomicBoolean hideWindowCalled = new AtomicBoolean(false);
    final AtomicBoolean animationsEnabled = new AtomicBoolean(false);

    final MainKeyboardBackAnimationHelper helper =
        MainKeyboardBackAnimationHelper.create(
            () -> window,
            () -> container,
            () -> hideWindowCalled.set(true),
            animationsEnabled::get);

    Assert.assertNotNull(helper);
    final OnBackAnimationCallback callback = (OnBackAnimationCallback) helper.getCallback();
    Assert.assertNotNull(callback);

    callback.onBackStarted(new BackEvent(10f, 10f, 0.0f, BackEvent.EDGE_LEFT));
    callback.onBackProgressed(new BackEvent(50f, 10f, 0.5f, BackEvent.EDGE_LEFT));
    Assert.assertEquals(0f, container.getPredictiveBackProgress(), 0.01f);
    Assert.assertEquals(0f, container.getTranslationY(), 0.01f);
    Assert.assertEquals(1.0f, container.getAlpha(), 0.01f);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testNullSuppliersHandling() {
    final MainKeyboardBackAnimationHelper helper =
        MainKeyboardBackAnimationHelper.create(() -> null, () -> null, () -> {}, () -> true);
    Assert.assertNotNull(helper);
    helper.register();
    helper.unregister();

    final OnBackAnimationCallback callback = (OnBackAnimationCallback) helper.getCallback();
    Assert.assertNotNull(callback);
    callback.onBackStarted(new BackEvent(0, 0, 0, BackEvent.EDGE_LEFT));
    callback.onBackProgressed(new BackEvent(0, 0, 0.5f, BackEvent.EDGE_LEFT));
    callback.onBackCancelled();
    callback.onBackInvoked();
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testOnBackCancelledWithAnimationsDisabled() {
    final Window window = Mockito.mock(Window.class);
    final KeyboardViewContainerView container =
        (KeyboardViewContainerView)
            LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.main_keyboard_layout, null, false);
    container.layout(0, 0, 1000, 500);
    container.setPredictiveBackProgress(0.5f);

    final MainKeyboardBackAnimationHelper helper =
        MainKeyboardBackAnimationHelper.create(
            () -> window, () -> container, () -> {}, () -> false);

    Assert.assertNotNull(helper);
    final OnBackAnimationCallback callback = (OnBackAnimationCallback) helper.getCallback();
    Assert.assertNotNull(callback);
    callback.onBackStarted(new BackEvent(0, 0, 0, BackEvent.EDGE_LEFT));
    callback.onBackCancelled();
    Assert.assertEquals(0f, container.getPredictiveBackProgress(), 0.01f);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.TIRAMISU)
  public void testNullOnApi33() {
    final MainKeyboardBackAnimationHelper helper =
        MainKeyboardBackAnimationHelper.create(() -> null, () -> null, () -> {}, () -> true);

    Assert.assertNull(helper);
  }
}
