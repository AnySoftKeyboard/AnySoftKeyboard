package com.anysoftkeyboard.ime;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.R;

public abstract class AnySoftKeyboardColorizeNavBar extends AnySoftKeyboardIncognito {

  private boolean mPrefsToShow;

  private int mNavigationBarMinHeight;
  private int mExtraBottomPadding = 0;

  @Override
  public void onCreate() {
    super.onCreate();
    mNavigationBarMinHeight =
        getResources().getDimensionPixelOffset(R.dimen.navigation_bar_min_height);

    Logger.d(TAG, "Colorized nav-bar using WindowInsets (modern approach)");

    if (Build.VERSION.SDK_INT >= 35) {
      // Edge-to-edge is enforced on Android 15+, always enable colorized nav bar
      mPrefsToShow = true;
    } else {
      addDisposable(
          prefs()
              .getBoolean(
                  R.string.settings_key_colorize_nav_bar, R.bool.settings_default_colorize_nav_bar)
              .asObservable()
              .subscribe(
                  val -> {
                    mPrefsToShow = val;
                    // Re-setup WindowInsets when preference changes, but only if window is
                    // available
                    // This prevents issues in test environments or during initialization
                    if (getWindow() != null
                        && getWindow().getWindow() != null
                        && getInputViewContainer() != null) {
                      setupWindowInsets();
                    }
                  },
                  GenericOnError.onError("settings_key_colorize_nav_bar")));
    }

    addDisposable(
        prefs()
            .getInteger(
                R.string.settings_key_bottom_extra_padding_in_portrait,
                R.integer.settings_default_bottom_extra_padding_in_portrait)
            .asObservable()
            .subscribe(
                val ->
                    mExtraBottomPadding =
                        Math.max(0, (int) getResources().getDisplayMetrics().density * val),
                GenericOnError.onError("settings_key_bottom_extra_padding_in_portrait")));
  }

  private int getMinimumBottomPadding() {
    return (getCurrentOrientation() == Configuration.ORIENTATION_PORTRAIT ? mExtraBottomPadding : 0)
        + mNavigationBarMinHeight;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mNavigationBarMinHeight =
        getResources().getDimensionPixelOffset(R.dimen.navigation_bar_min_height);
    // WindowInsets will automatically be re-dispatched on configuration changes,
    // so no need to manually recalculate navigation bar height
  }

  @Override
  public void onStartInputView(EditorInfo info, boolean restarting) {
    super.onStartInputView(info, restarting);
    setupWindowInsets();
  }

  private void setupWindowInsets() {
    final var w = getWindow().getWindow();
    final var inputContainer = getInputViewContainer();

    if (w == null || inputContainer == null) {
      Logger.w(TAG, "Window or container null, skipping WindowInsets setup");
      return;
    }

    if (mPrefsToShow) {
      Logger.d(TAG, "Setting up WindowInsets-based colorized nav-bar");

      // Configure window for edge-to-edge on all API levels
      w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      w.setNavigationBarColor(Color.TRANSPARENT);
      w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // API 30+: Use modern WindowCompat API
        WindowCompat.setDecorFitsSystemWindows(w, false);
      } else {
        // API 29 and below: Use legacy system UI flags for edge-to-edge
        w.getDecorView()
            .setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
      }

      // Set up WindowInsets listener on the decor view
      // Using decor view is recommended for IME to ensure we get all system insets
      // Note: On API 30 in Robolectric test environments, WindowInsets handling has known issues
      // where returning any WindowInsetsCompat causes NPEs when converting back to platform types
      ViewCompat.setOnApplyWindowInsetsListener(
          w.getDecorView(),
          (v, windowInsets) -> {
            if (windowInsets == null) {
              // In test environments, WindowInsets may be null
              Logger.w(TAG, "WindowInsets is null, skipping padding update");
              return WindowInsetsCompat.CONSUMED;
            }

            try {
              // Get navigation bar insets from the system
              // Using fully qualified name to avoid conflict with InputMethodService.Insets
              final androidx.core.graphics.Insets navBarInsets =
                  windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());

              // Calculate the bottom padding, respecting minimum and user preferences
              final int bottomPadding = Math.max(navBarInsets.bottom, getMinimumBottomPadding());

              Logger.d(
                  TAG,
                  "WindowInsets applied: navBar.bottom=%d, minimumPadding=%d, final padding=%d",
                  navBarInsets.bottom,
                  getMinimumBottomPadding(),
                  bottomPadding);

              // Apply padding to the container
              inputContainer.setBottomPadding(bottomPadding);

              // Return insets unconsumed to allow other views to handle them if needed
              return windowInsets;
            } catch (Exception e) {
              // Handle any errors during insets processing (e.g., in test environments)
              Logger.w(TAG, "Error processing WindowInsets, applying minimum padding", e);
              inputContainer.setBottomPadding(getMinimumBottomPadding());
              return WindowInsetsCompat.CONSUMED;
            }
          });

    } else {
      Logger.d(TAG, "Clearing colorized nav-bar (prefs disabled)");
      // Remove the insets listener FIRST before clearing window settings
      // This prevents WindowInsets dispatch issues on API 30 in Robolectric
      ViewCompat.setOnApplyWindowInsetsListener(w.getDecorView(), null);
      clearColorizedNavBar(w, inputContainer);
    }
  }

  private void clearColorizedNavBar(
      @NonNull Window w, @NonNull KeyboardViewContainerView containerView) {
    // Clear flags on all API levels
    w.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    w.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      // API 30+: Clear modern WindowCompat setting
      WindowCompat.setDecorFitsSystemWindows(w, true);
    } else {
      // API 29 and below: Clear legacy system UI flags
      w.getDecorView().setSystemUiVisibility(0);
    }
    containerView.setBottomPadding(0);
  }
}
