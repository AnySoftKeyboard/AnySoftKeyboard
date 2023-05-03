package com.anysoftkeyboard.keyboards.views.extradraw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.SystemClock;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewWithExtraDraw;

public class TypingExtraDraw implements ExtraDraw {

  private final String mTypingText;
  private String mCurrentText;
  private long mNextLetterTime;
  private long mDurationPerLetter;
  private final PaintModifier<Float> mPaintModifier;
  private final long mStartTime;
  private final long mTotalDuration;
  private final Point mTypingCenterPoint;

  public TypingExtraDraw(
      String text, Point centerPoint, long durationPerLetter, PaintModifier<Float> paintModifier) {
    mTypingText = text;
    mCurrentText = mTypingText.substring(0, 1);
    mPaintModifier = paintModifier;
    mStartTime = SystemClock.elapsedRealtime();
    mNextLetterTime = durationPerLetter;
    mDurationPerLetter = durationPerLetter;
    mTotalDuration = (durationPerLetter * mTypingText.length());
    mTypingCenterPoint = centerPoint;
  }

  @Override
  public boolean onDraw(
      Canvas canvas, Paint originalPaint, AnyKeyboardViewWithExtraDraw parentKeyboardView) {
    final long currentAnimationTime = SystemClock.elapsedRealtime() - mStartTime;
    if (currentAnimationTime > mTotalDuration) {
      return false;
    } else {
      final float typingFraction = ((float) currentAnimationTime) / ((float) mTotalDuration);
      final Paint paint = mPaintModifier.modify(originalPaint, parentKeyboardView, typingFraction);
      if (currentAnimationTime >= mNextLetterTime) {
        mNextLetterTime += mDurationPerLetter;
        mCurrentText = mTypingText.substring(0, mCurrentText.length() + 1);
      }

      canvas.translate(mTypingCenterPoint.x, mTypingCenterPoint.y);
      canvas.drawText(mCurrentText, 0, mCurrentText.length(), 0, 0, paint);
      canvas.translate(-mTypingCenterPoint.x, -mTypingCenterPoint.y);

      return true;
    }
  }
}
