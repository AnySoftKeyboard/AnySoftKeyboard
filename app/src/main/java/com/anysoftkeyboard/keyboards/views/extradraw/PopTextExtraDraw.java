package com.anysoftkeyboard.keyboards.views.extradraw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.SystemClock;
import android.support.annotation.VisibleForTesting;

import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewWithExtraDraw;

public abstract class PopTextExtraDraw implements ExtraDraw {
    private static final int COMPLETE_POP_OUT_ANIMATION_DURATION = 1200;
    private final CharSequence mPopOutText;
    private final long mPopStartTime;
    private final Point mPopStartPoint;
    private final int mTargetEndYPosition;

    protected PopTextExtraDraw(CharSequence text, Point startPoint, int endYPosition, long popStartTime) {
        mPopOutText = text;
        mPopStartTime = popStartTime;
        mPopStartPoint = startPoint;
        mTargetEndYPosition = endYPosition;
    }

    @Override
    public boolean onDraw(Canvas canvas, Paint keyValuesPaint, AnyKeyboardViewWithExtraDraw parentKeyboardView) {
        final long currentAnimationTime = SystemClock.elapsedRealtime() - mPopStartTime;
        if (currentAnimationTime > COMPLETE_POP_OUT_ANIMATION_DURATION) {
            return false;
        } else {
            final float animationInterpolatorFraction = calculateAnimationInterpolatorFraction(((float) currentAnimationTime) / ((float) COMPLETE_POP_OUT_ANIMATION_DURATION));

            final int y = calculateCurrentYPosition(mPopStartPoint.y, mTargetEndYPosition, animationInterpolatorFraction);
            final int x = mPopStartPoint.x;
            final int alpha = (int) (255 * animationInterpolatorFraction);

            // drawing

            parentKeyboardView.setPaintToKeyText(keyValuesPaint);
            keyValuesPaint.setAlpha(255 - alpha);
            keyValuesPaint.setShadowLayer(5, 0, 0, Color.BLACK);
            keyValuesPaint.setTextSize(keyValuesPaint.getTextSize() * (1.0f + animationInterpolatorFraction));
            canvas.translate(x, y);
            canvas.drawText(mPopOutText, 0, mPopOutText.length(), 0, 0, keyValuesPaint);
            canvas.translate(-x, -y);

            return true;
        }
    }

    protected abstract int calculateCurrentYPosition(int startY, int endYPosition, float animationInterpolatorFraction);

    protected abstract float calculateAnimationInterpolatorFraction(float animationTimeFraction);

    public static class PopOut extends PopTextExtraDraw {
        private boolean mFinished;

        public PopOut(CharSequence text, Point startPoint, int endYPosition) {
            super(text, startPoint, endYPosition, SystemClock.elapsedRealtime());
        }

        @Override
        protected int calculateCurrentYPosition(int startY, int endYPosition, float animationInterpolatorFraction) {
            return startY - (int) ((startY - endYPosition) * animationInterpolatorFraction);
        }

        @Override
        protected float calculateAnimationInterpolatorFraction(float animationTimeFraction) {
            return (1.0f - (1.0f - animationTimeFraction) * (1.0f - animationTimeFraction));
        }

        @Override
        public boolean onDraw(Canvas canvas, Paint keyValuesPaint, AnyKeyboardViewWithExtraDraw parentKeyboardView) {
            if (mFinished) return false;

            mFinished = !super.onDraw(canvas, keyValuesPaint, parentKeyboardView);

            return !mFinished;
        }

        public ExtraDraw generateRevert() {
            if (mFinished) throw new IllegalStateException("Already in mFinished state!");

            mFinished = true;

            return new PopIn(super.mPopOutText,
                    new Point(super.mPopStartPoint.x, super.mTargetEndYPosition), super.mPopStartPoint.y,
                    SystemClock.elapsedRealtime() - super.mPopStartTime);
        }

        public boolean isDone() {
            return mFinished;
        }
    }

    public static class PopIn extends PopTextExtraDraw {

        public PopIn(CharSequence text, Point startPoint, int endYPosition, long popTimePassed) {
            super(text, startPoint, endYPosition, SystemClock.elapsedRealtime() - (COMPLETE_POP_OUT_ANIMATION_DURATION - popTimePassed));
        }

        @Override
        protected int calculateCurrentYPosition(int startY, int endYPosition, float animationInterpolatorFraction) {
            return endYPosition + (int) ((startY - endYPosition) * animationInterpolatorFraction);
        }

        @Override
        protected float calculateAnimationInterpolatorFraction(float animationTimeFraction) {
            return 1.0f - animationTimeFraction * animationTimeFraction;
        }
    }

    @VisibleForTesting
    public CharSequence getPopText() {
        return mPopOutText;
    }
}
