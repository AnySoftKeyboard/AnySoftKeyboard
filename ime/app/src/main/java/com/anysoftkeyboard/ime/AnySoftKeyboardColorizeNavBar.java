package com.anysoftkeyboard.ime;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.BoolRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.WindowCompat;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.R;

public abstract class AnySoftKeyboardColorizeNavBar extends AnySoftKeyboardIncognito {

  private static final int NO_ID = 0;

  @DimenRes private int mNavigationBarHeightId = NO_ID;
  @BoolRes private int mNavigationBarShownId = NO_ID;
  private boolean mPrefsToShow;

  private int mNavigationBarMinHeight;
  private int mExtraBottomPadding = 0;

  @Override
  public void onCreate() {
    super.onCreate();
    mNavigationBarMinHeight =
        getResources().getDimensionPixelOffset(R.dimen.navigation_bar_min_height);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      // use androidx.core.view.WindowInsetsCompat
      mNavigationBarHeightId =
          getResources().getIdentifier("navigation_bar_height", "dimen", "android");
      mNavigationBarShownId =
          getResources().getIdentifier("config_showNavigationBar", "bool", "android");

      Logger.d(
          TAG,
          "Colorized nav-bar resources: navigation_bar_height %d," + " config_showNavigationBar %d",
          mNavigationBarHeightId,
          mNavigationBarShownId);

      addDisposable(
          prefs()
              .getBoolean(
                  R.string.settings_key_colorize_nav_bar, R.bool.settings_default_colorize_nav_bar)
              .asObservable()
              .subscribe(
                  val -> mPrefsToShow = val,
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
                    mExtraBottomPadding = (int) (getResources().getDisplayMetrics().density * val),
                GenericOnError.onError("settings_key_bottom_extra_padding_in_portrait")));
  }

  private int getMinimumBottomPadding() {
    return (getCurrentOrientation() == Configuration.ORIENTATION_PORTRAIT ? mExtraBottomPadding : 0)
        + mNavigationBarMinHeight;
  }

  private boolean doesOsShowNavigationBar() {
    if (mNavigationBarShownId != NO_ID) {
      return getResources().getBoolean(mNavigationBarShownId);
    } else {
      return false;
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mNavigationBarMinHeight =
        getResources().getDimensionPixelOffset(R.dimen.navigation_bar_min_height);
  }

  @Override
  public void onStartInputView(EditorInfo info, boolean restarting) {
    super.onStartInputView(info, restarting);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      setColorizedNavBar();
    }
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  private void setColorizedNavBar() {
    final var w = getWindow().getWindow();
    final var inputContainer = getInputViewContainer();
    if (w != null && inputContainer != null) {
      final var doesOsShowNavigationBar = doesOsShowNavigationBar();
      if (doesOsShowNavigationBar) {
        final int navBarHeight = getNavBarHeight();
        if (navBarHeight > 0 && mPrefsToShow) {
          Logger.d(TAG, "Showing Colorized nav-bar with height %d", navBarHeight);
          w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
          w.setNavigationBarColor(Color.TRANSPARENT);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // we only want to do this in R or higher (although, a compat version exists
            // prior).
            // Using the Compat to better handle old devices
            WindowCompat.setDecorFitsSystemWindows(w, false);
          }
          inputContainer.setBottomPadding(Math.max(navBarHeight, getMinimumBottomPadding()));
        } else {
          Logger.d(
              TAG,
              "Not showing Colorized nav-bar with height %d and prefs %s",
              navBarHeight,
              mPrefsToShow);
          clearColorizedNavBar(w, inputContainer);
        }
      } else {
        Logger.w(TAG, "Will not show Colorized nav-bar since not doesOsShowNavigationBar");
        clearColorizedNavBar(w, inputContainer);
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  private void clearColorizedNavBar(
      @NonNull Window w, @NonNull KeyboardViewContainerView containerView) {
    w.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      // we only want to do this in R or higher (although, a compat version exists prior).
      // Using the Compat to better handle old devices
      WindowCompat.setDecorFitsSystemWindows(w, true);
    }
    containerView.setBottomPadding(0);
  }

  private int getNavBarHeight() {
    if (mNavigationBarHeightId != NO_ID) {
      return getResources().getDimensionPixelSize(mNavigationBarHeightId);
    } else {
      return 0;
    }
  }
}
