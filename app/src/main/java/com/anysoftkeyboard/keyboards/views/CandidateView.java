/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class CandidateView extends View {

    private static final String TAG = "ASK CandidateView";

    private static final int OUT_OF_BOUNDS_X_CORD = -1;
    private int mTouchX = OUT_OF_BOUNDS_X_CORD;
    private static final int MAX_SUGGESTIONS = 32;
    private final int[] mWordWidth = new int[MAX_SUGGESTIONS];
    private final int[] mWordX = new int[MAX_SUGGESTIONS];
    private static final int SCROLL_PIXELS = 20;
    private final ArrayList<CharSequence> mSuggestions = new ArrayList<>();
    private final Drawable mSelectionHighlight;
    private float mHorizontalGap;
    private int mColorNormal;
    private int mColorRecommended;
    private int mColorOther;
    private final Paint mPaint;
    private final TextPaint mTextPaint;
    private final GestureDetector mGestureDetector;
    private AnySoftKeyboard mService;
    private boolean mNoticing = false;
    private CharSequence mSelectedString;
    private CharSequence mJustAddedWord;
    private int mSelectedIndex;
    private boolean mTypedWordValid;
    private boolean mHaveMinimalSuggestion;
    private Rect mBgPadding;
    private Drawable mDivider;
    private Drawable mCloseDrawable;
    private boolean mScrolled;
    private boolean mShowingAddToDictionary;
    private CharSequence mAddToDictionaryHint;
    private int mTargetScrollX;
    private int mTotalWidth;

    private boolean mAlwaysUseDrawText;
    @NonNull
    private Disposable mDisposable = Disposables.empty();

    public CandidateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Construct a CandidateView for showing suggested words for completion.
     */
    public CandidateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mSelectionHighlight = ContextCompat.getDrawable(context, R.drawable.list_selector_background_pressed);

        mAddToDictionaryHint = context.getString(R.string.hint_add_to_dictionary);

        mPaint = new Paint();
        mTextPaint = new TextPaint(mPaint);
        final int minTouchableWidth = context.getResources().getDimensionPixelOffset(R.dimen.candidate_min_touchable_width);
        mGestureDetector = new GestureDetector(context, new CandidateStripGestureListener(minTouchableWidth));

        setWillNotDraw(false);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        scrollTo(0, getScrollY());
    }

    public void setKeyboardTheme(@NonNull KeyboardTheme theme) {
        final Context context = getContext();
        final AddOn.AddOnResourceMapping remoteAttrs = theme.getResourceMapping();
        final int[] remoteStyleableArray = remoteAttrs.getRemoteStyleableArrayFromLocal(R.styleable.AnyKeyboardViewTheme);
        TypedArray a = theme.getPackageContext().obtainStyledAttributes(theme.getThemeResId(), remoteStyleableArray);
        mColorNormal = ContextCompat.getColor(context, R.color.candidate_normal);
        mColorOther = ContextCompat.getColor(context, R.color.candidate_other);
        mColorRecommended = ContextCompat.getColor(context, R.color.candidate_recommended);
        mHorizontalGap = context.getResources().getDimensionPixelSize(R.dimen.candidate_strip_x_gap);
        mDivider = null;
        mCloseDrawable = null;
        setBackgroundDrawable(null);
        setBackgroundColor(Color.BLACK);
        float fontSizePixel = context.getResources().getDimensionPixelSize(R.dimen.candidate_font_height);

        final int resolvedAttrsCount = a.getIndexCount();
        for (int attrIndex = 0; attrIndex < resolvedAttrsCount; attrIndex++) {
            final int remoteIndex = a.getIndex(attrIndex);
            try {
                //CHECKSTYLE:OFF: missingswitchdefault
                switch (remoteAttrs.getLocalAttrId(remoteStyleableArray[remoteIndex])) {
                    case R.attr.suggestionNormalTextColor:
                        mColorNormal = a.getColor(remoteIndex, mColorNormal);
                        break;
                    case R.attr.suggestionRecommendedTextColor:
                        mColorRecommended = a.getColor(remoteIndex, mColorRecommended);
                        break;
                    case R.attr.suggestionOthersTextColor:
                        mColorOther = a.getColor(remoteIndex, mColorOther);
                        break;
                    case R.attr.suggestionDividerImage:
                        mDivider = a.getDrawable(remoteIndex);
                        break;
                    case R.attr.suggestionCloseImage:
                        mCloseDrawable = a.getDrawable(remoteIndex);
                        break;
                    case R.attr.suggestionTextSize:
                        fontSizePixel = a.getDimension(remoteIndex, fontSizePixel);
                        break;
                    case R.attr.suggestionWordXGap:
                        mHorizontalGap = a.getDimension(remoteIndex, mHorizontalGap);
                        break;
                    case R.attr.suggestionBackgroundImage:
                        final Drawable stripImage = a.getDrawable(remoteIndex);
                        if (stripImage != null) {
                            setBackgroundColor(Color.TRANSPARENT);
                            setBackgroundDrawable(stripImage);
                        }
                        break;
                }
                //CHECKSTYLE:ON: missingswitchdefault
            } catch (Exception e) {
                Logger.w(TAG, "Got an exception while reading theme data", e);
            }
        }
        a.recycle();

        if (mDivider == null) {
            mDivider = ContextCompat.getDrawable(context, R.drawable.dark_suggestions_divider);
        }
        if (mCloseDrawable == null) {
            mCloseDrawable = ContextCompat.getDrawable(context, R.drawable.close_suggestions_strip_icon);
        }
        mPaint.setColor(mColorNormal);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(fontSizePixel);
        mPaint.setStrokeWidth(0);
        mPaint.setTextAlign(Align.CENTER);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDisposable = AnyApplication.prefs(getContext()).getBoolean(R.string.settings_key_workaround_disable_rtl_fix, R.bool.settings_default_workaround_disable_rtl_fix)
                .asObservable().subscribe(
                        value -> mAlwaysUseDrawText = value,
                        GenericOnError.onError("Failed reading settings_key_workaround_disable_rtl_fix in CandidateView."));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDisposable.dispose();
    }

    /**
     * A connection back to the service to communicate with the text field
     */
    public void setService(AnySoftKeyboard listener) {
        mService = listener;
    }

    @Override
    public int computeHorizontalScrollRange() {
        return mTotalWidth;
    }

    /**
     * If the canvas is null, then only touch calculations are performed to pick
     * the target candidate.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas != null) {
            super.onDraw(canvas);
        }
        mTotalWidth = 0;

        final int height = getHeight();
        if (mBgPadding == null) {
            mBgPadding = new Rect(0, 0, 0, 0);
            if (getBackground() != null) {
                getBackground().getPadding(mBgPadding);
            }
            mDivider.setBounds(0, 0, mDivider.getIntrinsicWidth(),
                    mDivider.getIntrinsicHeight());
        }

        final int dividerYOffset = (height - mDivider.getMinimumHeight()) / 2;
        final int count = mSuggestions.size();
        final Rect bgPadding = mBgPadding;
        final Paint paint = mPaint;
        final int touchX = mTouchX;
        final int scrollX = getScrollX();
        final boolean scrolled = mScrolled;
        final boolean typedWordValid = mTypedWordValid;

        int x = 0;
        for (int i = 0; i < count; i++) {
            CharSequence suggestion = mSuggestions.get(i);
            if (suggestion == null) {
                continue;
            }
            final int wordLength = suggestion.length();

            paint.setColor(mColorNormal);
            if (mHaveMinimalSuggestion &&
                    ((i == 1 && !typedWordValid) || (i == 0 && typedWordValid))) {
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                paint.setColor(mColorRecommended);
                // existsAutoCompletion = true;
            } else if (i != 0 || (wordLength == 1 && count > 1)) {
                // HACK: even if i == 0, we use mColorOther when this
                // suggestion's length is 1 and
                // there are multiple suggestions, such as the default
                // punctuation list.
                paint.setColor(mColorOther);
            }

            // now that we set the typeFace, we can measure
            int wordWidth;
            if ((wordWidth = mWordWidth[i]) == 0) {
                float textWidth = paint.measureText(suggestion, 0, wordLength);
                // wordWidth = Math.max(0, (int) textWidth + X_GAP * 2);
                wordWidth = (int) (textWidth + mHorizontalGap * 2);
                mWordWidth[i] = wordWidth;
            }

            mWordX[i] = x;

            if (touchX != OUT_OF_BOUNDS_X_CORD && !scrolled
                    && touchX + scrollX >= x
                    && touchX + scrollX < x + wordWidth) {
                if (canvas != null && !mShowingAddToDictionary) {
                    canvas.translate(x, 0);
                    mSelectionHighlight.setBounds(0, bgPadding.top, wordWidth,
                            height);
                    mSelectionHighlight.draw(canvas);
                    canvas.translate(-x, 0);
                }
                mSelectedString = suggestion;
                mSelectedIndex = i;
            }

            if (canvas != null) {
                // (+)This is the trick to get RTL/LTR text correct
                if (mAlwaysUseDrawText) {
                    final int y = (int) (height + paint.getTextSize() - paint.descent()) / 2;
                    canvas.drawText(suggestion, 0, wordLength, x + wordWidth / 2, y, paint);
                } else {
                    final int y = (int) (height - paint.getTextSize() + paint.descent()) / 2;
                    // no matter what: StaticLayout
                    float textX = x + (wordWidth / 2) - mHorizontalGap;
                    float textY = y - bgPadding.bottom - bgPadding.top;

                    canvas.translate(textX, textY);
                    mTextPaint.setTypeface(paint.getTypeface());
                    mTextPaint.setColor(paint.getColor());

                    StaticLayout suggestionText = new StaticLayout(suggestion,
                            mTextPaint, wordWidth, Alignment.ALIGN_CENTER,
                            1.0f, 0.0f, false);
                    suggestionText.draw(canvas);

                    canvas.translate(-textX, -textY);
                }
                // (-)
                paint.setColor(mColorOther);
                canvas.translate(x + wordWidth, 0);
                // Draw a divider unless it's after the hint
                //or the last suggested word
                if (count > 1 && (!mShowingAddToDictionary) && i != (count - 1)) {
                    canvas.translate(0, dividerYOffset);
                    mDivider.draw(canvas);
                    canvas.translate(0, -dividerYOffset);
                }
                canvas.translate(-x - wordWidth, 0);
            }
            paint.setTypeface(Typeface.DEFAULT);
            x += wordWidth;
        }
        mTotalWidth = x;
        if (mTargetScrollX != scrollX) {
            scrollToTarget();
        }
    }

    private void scrollToTarget() {
        int scrollX = getScrollX();
        if (mTargetScrollX > scrollX) {
            scrollX += SCROLL_PIXELS;
            if (scrollX >= mTargetScrollX) {
                scrollX = mTargetScrollX;
                scrollTo(scrollX, getScrollY());
                requestLayout();
            } else {
                scrollTo(scrollX, getScrollY());
            }
        } else {
            scrollX -= SCROLL_PIXELS;
            if (scrollX <= mTargetScrollX) {
                scrollX = mTargetScrollX;
                scrollTo(scrollX, getScrollY());
                requestLayout();
            } else {
                scrollTo(scrollX, getScrollY());
            }
        }
        invalidate();
    }

    /**
     * Setup what's to display in the suggestions strip
     *
     * @param suggestions           the list of words to show
     * @param typedWordValid        the typed word (word at index 0) is a valid word
     * @param haveMinimalSuggestion the list of suggestions contains a valid word. So, either
     *                              highlight the first word (typedWordValid == true), or
     *                              highlight the second word (typedWordValid != true)
     */
    public void setSuggestions(@NonNull List<? extends CharSequence> suggestions, boolean typedWordValid, boolean haveMinimalSuggestion) {
        clear();
        int insertCount = Math.min(suggestions.size(), MAX_SUGGESTIONS);
        for (CharSequence suggestion : suggestions) {
            mSuggestions.add(suggestion);
            if (--insertCount == 0) {
                break;
            }
        }

        mTypedWordValid = typedWordValid;
        scrollTo(0, getScrollY());
        mTargetScrollX = 0;
        mHaveMinimalSuggestion = haveMinimalSuggestion;
        //re-drawing required.
        invalidate();
    }

    public void showAddToDictionaryHint(CharSequence word) {
        ArrayList<CharSequence> suggestions = new ArrayList<>();
        suggestions.add(word);
        suggestions.add(mAddToDictionaryHint);
        setSuggestions(suggestions, false, false);
        mShowingAddToDictionary = true;
    }

    public boolean dismissAddToDictionaryHint() {
        if (!mShowingAddToDictionary) {
            return false;
        }
        clear();
        return true;
    }

    public List<CharSequence> getSuggestions() {
        return mSuggestions;
    }

    public void clear() {
        // Don't call mSuggestions.clear() because it's being used for logging
        // in LatinIME.pickSuggestionManually().
        mSuggestions.clear();
        mNoticing = false;
        mTouchX = OUT_OF_BOUNDS_X_CORD;
        mSelectedString = null;
        mSelectedIndex = -1;
        mShowingAddToDictionary = false;
        invalidate();
        Arrays.fill(mWordWidth, 0);
        Arrays.fill(mWordX, 0);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent me) {
        if (mGestureDetector.onTouchEvent(me)) {
            return true;
        }

        int action = me.getAction();
        final int x = (int) me.getX();
        final int y = (int) me.getY();
        mTouchX = x;

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                if (y <= 0) {
                    // Fling up!?
                    //Fling up should be a hacker's way to delete words (user dictionary words)
                    if (mSelectedString != null) {
                        Logger.d(TAG, "Fling up from candidates view. Deleting word at index %d, which is %s", mSelectedIndex, mSelectedString);
                        mService.removeFromUserDictionary(mSelectedString.toString());
                        clear();//clear also calls invalidate().
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mScrolled) {
                    if (mSelectedString != null) {
                        if (mShowingAddToDictionary) {
                            final CharSequence word = mSuggestions.get(0);
                            if (word.length() >= 2 && !mNoticing) {
                                Logger.d(TAG, "User wants to add the word '%s' to the user-dictionary.", word);
                                mService.addWordToDictionary(word.toString());
                            }
                        } else if (!mNoticing) {
                            mService.pickSuggestionManually(mSelectedIndex, mSelectedString);
                        } else if (mSelectedIndex == 1 && !TextUtils.isEmpty(mJustAddedWord)) {
                            // 1 is the index of "Remove?"
                            Logger.d(TAG, "User wants to remove an added word '%s'", mJustAddedWord);
                            mService.removeFromUserDictionary(mJustAddedWord.toString());
                        }
                    }
                }
                //allowing fallthrough to call invalidate.
            case MotionEvent.ACTION_DOWN:
            default:
                invalidate();
                break;
        }
        return true;
    }

    public void notifyAboutWordAdded(CharSequence word) {
        mJustAddedWord = word;
        ArrayList<CharSequence> notice = new ArrayList<>(2);
        notice.add(getContext().getResources().getString(R.string.added_word, mJustAddedWord));
        notice.add(getContext().getResources().getString(R.string.revert_added_word_question));
        setSuggestions(notice, true, false);
        mNoticing = true;
    }

    public void notifyAboutRemovedWord(CharSequence word) {
        mJustAddedWord = null;
        ArrayList<CharSequence> notice = new ArrayList<>(1);
        notice.add(getContext().getResources().getString(R.string.removed_word, word));
        setSuggestions(notice, true, false);
        mNoticing = true;
    }

    public void replaceTypedWord(CharSequence typedWord) {
        if (mSuggestions.size() > 0) {
            mSuggestions.set(0, typedWord);
            invalidate();
        }
    }

    public int getTextOthersColor() {
        return mColorOther;
    }

    public float getTextSize() {
        return mPaint.getTextSize();
    }

    public Drawable getCloseIcon() {
        return mCloseDrawable;
    }

    private class CandidateStripGestureListener extends
            GestureDetector.SimpleOnGestureListener {
        private final int mTouchSlopSquare;

        public CandidateStripGestureListener(int touchSlop) {
            // Slightly reluctant to scroll to be able to easily choose the
            // suggestion
            mTouchSlopSquare = touchSlop * touchSlop;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mScrolled = false;
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            if (!mScrolled) {
                // This is applied only when we recognize that scrolling is
                // starting.
                final int deltaX = (int) (e2.getX() - e1.getX());
                final int deltaY = (int) (e2.getY() - e1.getY());
                final int distance = (deltaX * deltaX) + (deltaY * deltaY);
                if (distance < mTouchSlopSquare) {
                    return true;
                }
                mScrolled = true;
            }

            final int width = getWidth();
            mScrolled = true;
            int scrollX = getScrollX();
            scrollX += (int) distanceX;
            if (scrollX < 0) {
                scrollX = 0;
            }
            if (distanceX > 0 && scrollX + width > mTotalWidth) {
                scrollX -= (int) distanceX;
            }
            mTargetScrollX = scrollX;
            scrollTo(scrollX, getScrollY());
            // hidePreview();
            invalidate();
            return true;
        }
    }
}