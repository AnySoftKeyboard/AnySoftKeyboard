package com.anysoftkeyboard.ime;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.anysoftkeyboard.android.NightMode;
import com.anysoftkeyboard.overlay.OverlayDataImpl;
import com.anysoftkeyboard.overlay.OverlyDataCreator;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;

public abstract class AnySoftKeyboardNightMode extends AnySoftKeyboardThemeOverlay {

  private boolean mNightMode;
  private ToggleOverlayCreator mToggleOverlayCreator;

  @Override
  public void onCreate() {
    super.onCreate();

    addDisposable(
        NightMode.observeNightModeState(getApplicationContext(), 0, R.bool.settings_default_true)
            .subscribe(
                powerState -> {
                  mNightMode = powerState;
                  setupInputViewWatermark();
                },
                GenericOnError.onError("night-mode icon")));

    addDisposable(
        NightMode.observeNightModeState(
                getApplicationContext(),
                R.string.settings_key_night_mode_theme_control,
                R.bool.settings_default_false)
            .subscribe(
                mToggleOverlayCreator::setToggle, GenericOnError.onError("night-mode theme")));
  }

  @NonNull @Override
  protected List<Drawable> generateWatermark() {
    final List<Drawable> watermark = super.generateWatermark();
    if (mNightMode) {
      watermark.add(ContextCompat.getDrawable(this, R.drawable.ic_watermark_night_mode));
    }
    return watermark;
  }

  @Override
  protected OverlyDataCreator createOverlayDataCreator() {
    return mToggleOverlayCreator =
        new ToggleOverlayCreator(
            super.createOverlayDataCreator(),
            this,
            new OverlayDataImpl(0xFF222222, 0xFF000000, Color.DKGRAY, Color.GRAY, Color.DKGRAY),
            "NightMode");
  }
}
