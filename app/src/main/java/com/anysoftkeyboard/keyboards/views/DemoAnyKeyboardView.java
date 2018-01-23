package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.theme.KeyboardTheme;

import java.lang.ref.WeakReference;

/**
 * Will render the keyboard view but will not provide ANY interactivity.
 */
public class DemoAnyKeyboardView extends AnyKeyboardView {
    private TypingSimulator mTypingSimulator;
    @Nullable
    private OnViewBitmapReadyListener mOnViewBitmapReadyListener = null;
    private final int mInitialKeyboardWidth;
    private float mKeyboardScale = 1f;

    public DemoAnyKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoAnyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTypingSimulator = new TypingSimulator(this);
        mInitialKeyboardWidth = getThemedKeyboardDimens().getKeyboardMaxWidth();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        AnyKeyboard keyboard = getKeyboard();
        if (keyboard == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int width = keyboard.getMinWidth() + getPaddingLeft() + getPaddingRight();
            if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
                width = MeasureSpec.getSize(widthMeasureSpec);
                mKeyboardScale = ((float) width) / mInitialKeyboardWidth;
            } else {
                mKeyboardScale = 1f;
            }
            int height = keyboard.getHeight() + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension((int) (width / mKeyboardScale), (int) (height * mKeyboardScale));
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(mKeyboardScale, mKeyboardScale);
        super.onDraw(canvas);
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
                final long downEventTime = eventTime - (isDownEvent ? 0 : TypingSimulator.KEY_DOWN_DELAY);
                MotionEvent motionEvent = MotionEvent.obtain(downEventTime, eventTime,
                        isDownEvent ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP,
                        key.x + key.width / 2, key.y + key.height / 2, 0);
                super.onTouchEvent(motionEvent);
                motionEvent.recycle();
            }
        }
    }

    private void simulateCancelTouchEvent() {
        final long eventTime = SystemClock.uptimeMillis();
        MotionEvent motionEvent = MotionEvent.obtain(eventTime, eventTime, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        super.onTouchEvent(motionEvent);
        motionEvent.recycle();
    }

    public void setOnViewBitmapReadyListener(OnViewBitmapReadyListener listener) {
        mOnViewBitmapReadyListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final OnViewBitmapReadyListener listener = mOnViewBitmapReadyListener;
        if (changed && listener != null && getWidth() > 0 && getHeight() > 0) {
            final Bitmap bitmap = generateBitmapFromView();
            if (bitmap != null) {
                listener.onViewBitmapReady(bitmap);
            }
        }
    }

    private Bitmap generateBitmapFromView() {
        Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        draw(c);
        return b;
    }

    @Override
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTypingSimulator.onViewDetach();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        mTypingSimulator.onViewDetach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        mTypingSimulator.onViewAttach();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mTypingSimulator.onViewAttach();
    }

    private static class TypingSimulator extends Handler {
        private static final long INITIAL_DELAY = 512;
        private static final long NEXT_KEY_DELAY = 256;
        private static final long NEXT_KEY_SPACE_DELAY = 512;
        private static final long NEXT_CYCLE_DELAY = 1024;
        private static final long KEY_DOWN_DELAY = 128;

        private static final int PRESS_MESSAGE = 109;
        private static final int RELEASE_MESSAGE = 110;
        private static final int CANCEL_MESSAGE = 111;

        private final WeakReference<DemoAnyKeyboardView> mDemoAnyKeyboardViewWeakReference;
        @NonNull
        private String mTextToSimulate = "";
        private int mSimulationIndex = 0;
        private boolean mIsEnabled;

        private TypingSimulator(@NonNull DemoAnyKeyboardView keyboardView) {
            mDemoAnyKeyboardViewWeakReference = new WeakReference<>(keyboardView);
        }

        public void startSimulating(@NonNull String textToSimulate) {
            stopSimulating();
            mTextToSimulate = textToSimulate;
            if (!TextUtils.isEmpty(mTextToSimulate))
                sendMessageDelayed(obtainMessage(PRESS_MESSAGE), INITIAL_DELAY);
        }

        public void stopSimulating() {
            clearPressMessages();
            mTextToSimulate = "";
            mSimulationIndex = 0;
        }

        private void clearPressMessages() {
            removeMessages(PRESS_MESSAGE);
            removeMessages(RELEASE_MESSAGE);
            removeMessages(CANCEL_MESSAGE);
        }

        @Override
        public void handleMessage(Message msg) {
            DemoAnyKeyboardView keyboardView = mDemoAnyKeyboardViewWeakReference.get();
            if (keyboardView == null || mTextToSimulate.length() == 0) return;
            final char keyToSimulate = mTextToSimulate.charAt(mSimulationIndex);
            switch (msg.what) {
                case PRESS_MESSAGE:
                    if (mIsEnabled) keyboardView.simulateKeyTouchEvent(keyToSimulate, true);
                    if (mIsEnabled)
                        sendMessageDelayed(obtainMessage(RELEASE_MESSAGE), KEY_DOWN_DELAY);
                    break;
                case RELEASE_MESSAGE:
                    //sending RELEASE even if we are disabled
                    keyboardView.simulateKeyTouchEvent(keyToSimulate, false);
                    mSimulationIndex++;
                    if (mSimulationIndex == mTextToSimulate.length()) {
                        mSimulationIndex = 0;
                        if (mIsEnabled)
                            sendMessageDelayed(obtainMessage(PRESS_MESSAGE), NEXT_CYCLE_DELAY);
                    } else {
                        if (mIsEnabled)
                            sendMessageDelayed(obtainMessage(PRESS_MESSAGE), (keyToSimulate == ' ') ? NEXT_KEY_SPACE_DELAY : NEXT_KEY_DELAY);
                    }
                    break;
                case CANCEL_MESSAGE:
                    keyboardView.simulateCancelTouchEvent();
                    keyboardView.closing();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

        public void onViewDetach() {
            if (!mIsEnabled) return;

            mIsEnabled = false;
            clearPressMessages();
            sendMessage(obtainMessage(CANCEL_MESSAGE));
        }


        public void onViewAttach() {
            if (mIsEnabled) return;
            mIsEnabled = true;
            startSimulating(mTextToSimulate);
        }
    }

    public interface OnViewBitmapReadyListener {
        void onViewBitmapReady(Bitmap bitmap);
    }
}
