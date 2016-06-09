package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.R;

import java.lang.ref.WeakReference;

/**
 * Will render the keyboard view but will not provide ANY interactivity.
 */
public class DemoAnyKeyboardView extends AnyKeyboardView {

    private TypingSimulator mTypingSimulator;
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
        mTypingSimulator = new TypingSimulator(this);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent me) {
        //not handling ANY touch event.
        return false;
    }

    private void simulateKeyTouchEvent(char keyChar, boolean isDownEvent) {
        final AnyKeyboard keyboard = getKeyboard();
        if (keyboard == null) return;

        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.getPrimaryCode() == keyChar) {
                final long eventTime = SystemClock.uptimeMillis();
                final long downEventTime = eventTime - (isDownEvent? 0 : TypingSimulator.KEY_DOWN_DELAY);
                MotionEvent motionEvent = MotionEvent.obtain(downEventTime, eventTime,
                        isDownEvent? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP,
                        key.x+key.width/2, key.y + key.height/2, 0);
                super.onTouchEvent(motionEvent);
            }
        }
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
        //it was protected, and now, MAGIC, it is public
        super.resetKeyboardTheme(theme);
    }

    public void setSimulatedTypingText(@Nullable String textToSimulate) {
        if (TextUtils.isEmpty(textToSimulate)) {
            mTypingSimulator.stopSimulating();
        } else {
            mTypingSimulator.startSimulating(textToSimulate);
        }
    }

    private static class TypingSimulator extends Handler {
        private static final long INITIAL_DELAY = 512;
        private static final long NEXT_KEY_DELAY = 256;
        private static final long NEXT_KEY_SPACE_DELAY = 512;
        private static final long NEXT_CYCLE_DELAY = 1024;
        private static final long KEY_DOWN_DELAY = 128;

        private static final int PRESS_MESSAGE = 109;
        private static final int RELEASE_MESSAGE = 110;

        private final WeakReference<DemoAnyKeyboardView> mDemoAnyKeyboardViewWeakReference;
        @NonNull
        private String mTextToSimulate = "";
        private int mSimulationIndex = 0;

        private TypingSimulator(@NonNull  DemoAnyKeyboardView keyboardView) {
            mDemoAnyKeyboardViewWeakReference = new WeakReference<>(keyboardView);
        }

        public void startSimulating(@NonNull String textToSimulate) {
            stopSimulating();
            mTextToSimulate = textToSimulate;
            sendMessageDelayed(obtainMessage(PRESS_MESSAGE), INITIAL_DELAY);
        }

        public void stopSimulating() {
            removeMessages(PRESS_MESSAGE);
            removeMessages(RELEASE_MESSAGE);
            mSimulationIndex = 0;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DemoAnyKeyboardView keyboardView = mDemoAnyKeyboardViewWeakReference.get();
            if (keyboardView == null) return;
            final char keyToSimulate = mTextToSimulate.charAt(mSimulationIndex);
            switch (msg.what) {
                case PRESS_MESSAGE:
                    keyboardView.simulateKeyTouchEvent(keyToSimulate, true);
                    sendMessageDelayed(obtainMessage(RELEASE_MESSAGE), KEY_DOWN_DELAY);
                    break;
                case RELEASE_MESSAGE:
                    keyboardView.simulateKeyTouchEvent(keyToSimulate, false);
                    mSimulationIndex++;
                    if (mSimulationIndex == mTextToSimulate.length()) {
                        mSimulationIndex = 0;
                        sendMessageDelayed(obtainMessage(PRESS_MESSAGE), NEXT_CYCLE_DELAY);
                    } else {
                        sendMessageDelayed(obtainMessage(PRESS_MESSAGE), (keyToSimulate == ' ')? NEXT_KEY_SPACE_DELAY : NEXT_KEY_DELAY);
                    }
                    break;
            }
        }
    }
}
