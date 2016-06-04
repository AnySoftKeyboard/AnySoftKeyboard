package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.R;

/**
 * Will render the keyboard view but will not provide ANY interactivity.
 */
public class DemoAnyKeyboardView extends AnyKeyboardView {

    private AsyncTask<Bitmap, Void, Palette.Swatch> mPaletteTask;

    public DemoAnyKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoAnyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //adding the padding of the main-ui
        final int extraUiPadding = (2 * getResources().getDimensionPixelOffset(R.dimen.global_content_padding_side));
        final int extraMarginFromParentBackground = (2 * getResources().getDimensionPixelOffset(R.dimen.keyboard_demo_shadow_padding));
        mKeyboardDimens.setKeyboardMaxWidth(mKeyboardDimens.getKeyboardMaxWidth() - extraUiPadding - extraMarginFromParentBackground);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent me) {
        //not handling ANY touch event.
        return false;
    }

    public void startPaletteTask(AsyncTask<Bitmap, Void, Palette.Swatch> paletteTask) {
        mPaletteTask = paletteTask;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed && mPaletteTask != null && getWidth() > 0 && getHeight() > 0) {
            final Bitmap bitmap = generateBitmapFromView();
            if (bitmap != null) {
                AsyncTaskCompat.executeParallel(mPaletteTask, bitmap);
                mPaletteTask = null;
            }
        }
    }

    @Nullable
    private Bitmap generateBitmapFromView() {
        Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        draw(c);
        return b;
    }

    public void resetKeyboardTheme(@NonNull KeyboardTheme theme) {
        super.resetKeyboardTheme(theme);
    }
}
