package com.anysoftkeyboard.ime;

import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.base.utils.CompatUtils;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlayDataImpl;
import com.anysoftkeyboard.overlay.OverlayDataNormalizer;
import com.anysoftkeyboard.overlay.OverlayDataOverrider;
import com.anysoftkeyboard.overlay.OverlyDataCreator;
import com.anysoftkeyboard.overlay.OverlyDataCreatorForAndroid;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.R;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public abstract class AnySoftKeyboardThemeOverlay extends AnySoftKeyboardKeyboardTagsSearcher {
  @VisibleForTesting static final OverlayData INVALID_OVERLAY_DATA = new EmptyOverlayData();

  private OverlyDataCreator mOverlyDataCreator;
  private String mLastOverlayPackage = "";
  protected KeyboardTheme mCurrentTheme;

  private static Map<String, OverlayData> createOverridesForOverlays() {
    return Collections.emptyMap();
  }

  private boolean mApplyRemoteAppColors;
  @NonNull private OverlayData mCurrentOverlayData = INVALID_OVERLAY_DATA;

  @Override
  public void onCreate() {
    super.onCreate();
    mOverlyDataCreator = createOverlayDataCreator();

    addDisposable(
        KeyboardThemeFactory.observeCurrentTheme(getApplicationContext())
            .subscribe(
                this::onThemeChanged,
                GenericOnError.onError("KeyboardThemeFactory.observeCurrentTheme")));

    addDisposable(
        prefs()
            .getBoolean(
                R.string.settings_key_apply_remote_app_colors,
                R.bool.settings_default_apply_remote_app_colors)
            .asObservable()
            .subscribe(
                enabled -> {
                  mApplyRemoteAppColors = enabled;
                  mCurrentOverlayData = INVALID_OVERLAY_DATA;
                  mLastOverlayPackage = "";
                  hideWindow();
                },
                GenericOnError.onError("settings_key_apply_remote_app_colors")));
  }

  protected void onThemeChanged(@NonNull KeyboardTheme theme) {
    mCurrentTheme = theme;

    // we'll need to reload the keyboards
    // TODO(vitalipom) - here recreate the current keyboard and clear all the others

    // and set the theme in the view
    final KeyboardViewContainerView inputViewContainer = getInputViewContainer();
    if (inputViewContainer != null) {
      inputViewContainer.setKeyboardTheme(mCurrentTheme);
      inputViewContainer.setThemeOverlay(mCurrentOverlayData);
    }
  }

  protected OverlyDataCreator createOverlayDataCreator() {
    return new OverlyDataCreator() {
      private final OverlyDataCreator mActualCreator =
          new OverlayDataOverrider(
              new OverlayDataNormalizer(
                  new OverlyDataCreatorForAndroid.Light(AnySoftKeyboardThemeOverlay.this), 96),
              createOverridesForOverlays());

      @Override
      public OverlayData createOverlayData(ComponentName remoteApp) {
        if (mApplyRemoteAppColors) {
          if (CompatUtils.objectEquals(remoteApp.getPackageName(), mLastOverlayPackage)) {
            return mCurrentOverlayData;
          } else {
            mLastOverlayPackage = remoteApp.getPackageName();
            return mActualCreator.createOverlayData(remoteApp);
          }
        } else {
          return INVALID_OVERLAY_DATA;
        }
      }
    };
  }

  @Override
  public void onStartInputView(EditorInfo info, boolean restarting) {
    super.onStartInputView(info, restarting);

    applyThemeOverlay(info);
  }

  protected void applyThemeOverlay(EditorInfo info) {
    // Note: While Android 11+ (API 30) introduced package visibility restrictions, IMEs have
    // automatic visibility to the apps they're actively serving input to. This means
    // getLaunchIntentForPackage() works for EditorInfo.packageName without requiring
    // <queries> declarations, even though this behavior is not explicitly documented.
    // This has been verified to work in production on Android 11+.
    final Intent launchIntentForPackage =
        info.packageName == null
            ? null
            : getPackageManager().getLaunchIntentForPackage(info.packageName);
    if (launchIntentForPackage != null) {
      mCurrentOverlayData =
          mOverlyDataCreator.createOverlayData(launchIntentForPackage.getComponent());
    } else {
      mCurrentOverlayData = INVALID_OVERLAY_DATA;
      mLastOverlayPackage = "";
    }

    final KeyboardViewContainerView inputViewContainer = getInputViewContainer();
    if (inputViewContainer != null) {
      inputViewContainer.setThemeOverlay(mCurrentOverlayData);
    }
  }

  @Override
  public void onAddOnsCriticalChange() {
    mLastOverlayPackage = "";
    super.onAddOnsCriticalChange();
  }

  @Override
  public View onCreateInputView() {
    mLastOverlayPackage = "";
    final View view = super.onCreateInputView();
    final KeyboardViewContainerView inputViewContainer = getInputViewContainer();
    inputViewContainer.setKeyboardTheme(mCurrentTheme);
    inputViewContainer.setThemeOverlay(mCurrentOverlayData);

    return view;
  }

  private static class EmptyOverlayData extends OverlayDataImpl {
    @Override
    public boolean isValid() {
      return false;
    }
  }

  static class ToggleOverlayCreator implements OverlyDataCreator {
    private final OverlyDataCreator mOriginalCreator;
    private final OverlayData mOverrideData;
    private final String mOwner;
    private final AnySoftKeyboardThemeOverlay mOverlayController;
    private boolean mUseOverride;

    ToggleOverlayCreator(
        OverlyDataCreator originalCreator,
        AnySoftKeyboardThemeOverlay overlayController,
        OverlayData overrideData,
        String owner) {
      mOriginalCreator = originalCreator;
      mOverlayController = overlayController;
      mOverrideData = overrideData;
      mOwner = owner;
    }

    void setToggle(boolean useOverride) {
      mUseOverride = useOverride;

      final EditorInfo currentInputEditorInfo = mOverlayController.getCurrentInputEditorInfo();
      if (currentInputEditorInfo != null) {
        mOverlayController.applyThemeOverlay(currentInputEditorInfo);
      }
    }

    @Override
    public OverlayData createOverlayData(ComponentName remoteApp) {
      if (mUseOverride) {
        return mOverrideData;
      } else {
        return mOriginalCreator.createOverlayData(remoteApp);
      }
    }

    @NonNull
    @Override
    public String toString() {
      return String.format(Locale.ROOT, "ToggleOverlayCreator %s %s", mOwner, super.toString());
    }
  }
}
