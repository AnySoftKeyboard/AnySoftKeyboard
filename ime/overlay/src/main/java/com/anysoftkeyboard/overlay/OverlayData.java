package com.anysoftkeyboard.overlay;

public interface OverlayData {
  /** The remote app primary color for text. */
  int getPrimaryTextColor();

  /** The remote app secondary color for text. */
  int getSecondaryTextColor();

  /** The remote app accent (activated) color. */
  int getAccentColor();

  /** The remote app primary elements color. */
  int getPrimaryColor();

  /** The remote app darker-primary elements color. */
  int getPrimaryDarkColor();

  boolean isValid();
}
