/*
 * Copyright (c) 2026 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ime;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Dialog;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.Window;
import android.window.BackEvent;
import android.window.OnBackAnimationCallback;
import android.window.OnBackInvokedDispatcher;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardPredictiveBackApi34Test {

  private static class TestableAnySoftKeyboard extends SoftKeyboard {
    private Dialog mDialog;
    private KeyboardViewContainerView mContainer;
    private final AtomicBoolean mHideWindowCalled = new AtomicBoolean(false);
    private boolean mAnimationsEnabled = true;

    @Override
    public Dialog getWindow() {
      return mDialog;
    }

    @Override
    public KeyboardViewContainerView getInputViewContainer() {
      return mContainer;
    }

    @Override
    public void hideWindow() {
      mHideWindowCalled.set(true);
    }

    @Override
    protected boolean areAnimationsEnabled() {
      return mAnimationsEnabled;
    }
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testCreateDismissAnimationHelperWiresAllDependenciesOnApi34() {
    final Window window = Mockito.mock(Window.class);
    final OnBackInvokedDispatcher dispatcher = Mockito.mock(OnBackInvokedDispatcher.class);
    Mockito.doReturn(dispatcher).when(window).getOnBackInvokedDispatcher();

    final Dialog dialog = Mockito.mock(Dialog.class);
    Mockito.doReturn(window).when(dialog).getWindow();

    final KeyboardViewContainerView container =
        (KeyboardViewContainerView)
            LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.main_keyboard_layout, null, false);
    container.layout(0, 0, 1000, 500);

    final TestableAnySoftKeyboard ime = new TestableAnySoftKeyboard();
    ime.mDialog = dialog;
    ime.mContainer = container;

    final MainKeyboardBackAnimationHelper helper = ime.createDismissAnimationHelper();
    Assert.assertNotNull(helper);
    Assert.assertTrue(
        helper instanceof MainKeyboardBackAnimationHelper.Api34MainKeyboardBackAnimationHelper);

    // Test idempotent registration
    helper.register();
    helper.register();
    Mockito.verify(dispatcher, Mockito.times(1))
        .registerOnBackInvokedCallback(
            Mockito.eq(OnBackInvokedDispatcher.PRIORITY_DEFAULT),
            Mockito.any(OnBackAnimationCallback.class));

    final OnBackAnimationCallback callback = (OnBackAnimationCallback) helper.getCallback();
    Assert.assertNotNull(callback);

    // Test back progress updates container via wired dependency
    callback.onBackStarted(new BackEvent(10f, 10f, 0.0f, BackEvent.EDGE_LEFT));
    callback.onBackProgressed(new BackEvent(50f, 10f, 0.5f, BackEvent.EDGE_LEFT));
    Assert.assertEquals(0.5f, container.getPredictiveBackProgress(), 0.01f);

    // Test back invocation triggers wired hideWindow
    callback.onBackInvoked();
    Assert.assertTrue(ime.mHideWindowCalled.get());
    Assert.assertEquals(0f, container.getPredictiveBackProgress(), 0.01f);

    // Test idempotent unregistration
    helper.unregister();
    helper.unregister();
    Mockito.verify(dispatcher, Mockito.times(1))
        .unregisterOnBackInvokedCallback(Mockito.eq(callback));
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testCreateDismissAnimationHelperWithNullDialog() {
    final TestableAnySoftKeyboard ime = new TestableAnySoftKeyboard();
    ime.mDialog = null;
    ime.mContainer = null;

    final MainKeyboardBackAnimationHelper helper = ime.createDismissAnimationHelper();
    Assert.assertNotNull(helper);

    // Should safely handle null dialog without throwing
    helper.register();
    helper.unregister();
  }
}
