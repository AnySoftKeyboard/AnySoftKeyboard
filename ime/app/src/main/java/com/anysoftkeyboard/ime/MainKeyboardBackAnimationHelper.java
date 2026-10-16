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

import android.os.Build;
import android.view.Window;
import android.window.BackEvent;
import android.window.OnBackAnimationCallback;
import android.window.OnBackInvokedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public interface MainKeyboardBackAnimationHelper {

  void register();

  void unregister();

  @Nullable
  Object getCallback();

  @Nullable
  static MainKeyboardBackAnimationHelper create(
      @NonNull Supplier<Window> windowSupplier,
      @NonNull Supplier<KeyboardViewContainerView> containerSupplier,
      @NonNull Runnable hideWindowRunnable,
      @NonNull BooleanSupplier animationsEnabledSupplier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      return new Api34MainKeyboardBackAnimationHelper(
          windowSupplier, containerSupplier, hideWindowRunnable, animationsEnabledSupplier);
    }
    return null;
  }

  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  class Api34MainKeyboardBackAnimationHelper implements MainKeyboardBackAnimationHelper {
    @NonNull private final Supplier<Window> mWindowSupplier;
    @NonNull private final Supplier<KeyboardViewContainerView> mContainerSupplier;
    @NonNull private final Runnable mHideWindowRunnable;
    @NonNull private final BooleanSupplier mAnimationsEnabledSupplier;
    @NonNull private final OnBackAnimationCallback mCallback;

    public Api34MainKeyboardBackAnimationHelper(
        @NonNull Supplier<Window> windowSupplier,
        @NonNull Supplier<KeyboardViewContainerView> containerSupplier,
        @NonNull Runnable hideWindowRunnable,
        @NonNull BooleanSupplier animationsEnabledSupplier) {
      mWindowSupplier = windowSupplier;
      mContainerSupplier = containerSupplier;
      mHideWindowRunnable = hideWindowRunnable;
      mAnimationsEnabledSupplier = animationsEnabledSupplier;
      mCallback = new KeyboardDismissBackAnimationCallback();
    }

    @Override
    public void register() {
      Window window = mWindowSupplier.get();
      if (window != null) {
        window
            .getOnBackInvokedDispatcher()
            .registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, mCallback);
      }
    }

    @Override
    public void unregister() {
      Window window = mWindowSupplier.get();
      if (window != null) {
        window.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(mCallback);
      }
      KeyboardViewContainerView container = mContainerSupplier.get();
      if (container != null) {
        container.resetPredictiveBackProgress();
      }
    }

    @Override
    @VisibleForTesting
    @NonNull
    public OnBackAnimationCallback getCallback() {
      return mCallback;
    }

    private class KeyboardDismissBackAnimationCallback implements OnBackAnimationCallback {
      private boolean mAnimationsEnabled = false;

      @Override
      public void onBackStarted(@NonNull BackEvent backEvent) {
        mAnimationsEnabled = mAnimationsEnabledSupplier.getAsBoolean();
        KeyboardViewContainerView container = mContainerSupplier.get();
        if (container != null) {
          container.cancelPredictiveBackAnimation();
        }
      }

      @Override
      public void onBackProgressed(@NonNull BackEvent backEvent) {
        KeyboardViewContainerView container = mContainerSupplier.get();
        if (container != null && mAnimationsEnabled) {
          container.setPredictiveBackProgress(backEvent.getProgress());
        }
      }

      @Override
      public void onBackCancelled() {
        KeyboardViewContainerView container = mContainerSupplier.get();
        if (container != null) {
          if (mAnimationsEnabled) {
            container.animatePredictiveBackCancel();
          } else {
            container.resetPredictiveBackProgress();
          }
        }
      }

      @Override
      public void onBackInvoked() {
        KeyboardViewContainerView container = mContainerSupplier.get();
        if (container != null) {
          container.resetPredictiveBackProgress();
        }
        mHideWindowRunnable.run();
      }
    }
  }
}
