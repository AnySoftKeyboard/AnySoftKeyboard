package com.anysoftkeyboard.keyboards.views;

import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.theme.KeyboardTheme;

public interface ThemeableChild {
    void setKeyboardTheme(KeyboardTheme theme);

    void setThemeOverlay(OverlayData overlay);
}
