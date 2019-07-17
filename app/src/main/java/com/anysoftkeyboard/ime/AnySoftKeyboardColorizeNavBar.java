package com.anysoftkeyboard.ime;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.DimenRes;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public abstract class AnySoftKeyboardColorizeNavBar extends AnySoftKeyboardIncognito {

    private static final int NO_ID = 0;

    @DimenRes
    private int mNavigationBarHeightId = NO_ID;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK) && !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)) {
                mNavigationBarHeightId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            }
        }
    }

    @Override
    public View onCreateInputView() {
        final View original = super.onCreateInputView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window w = getWindow().getWindow();
            if (w != null) {
                final int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    w.setNavigationBarColor(Color.TRANSPARENT);
                    getInputViewContainer().setBottomPadding(navBarHeight);
                }
            }
        }

        return original;
    }


    private int getNavBarHeight() {
        if (mNavigationBarHeightId != NO_ID && getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            return getResources().getDimensionPixelSize(mNavigationBarHeightId);
        } else {
            return 0;
        }
    }
}
