/*
 * Copyright (C) 2008 The Android Open Source Project
 * Copyright 2011 AnySoftKeyboard
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.anysoftkeyboard.keyboards.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class CandidateView extends View {

	private static final String TAG = "ASK CandidateView";

	// private static final int OUT_OF_BOUNDS_WORD_INDEX = -1;
	private static final int OUT_OF_BOUNDS_X_COORD = -1;

	private AnySoftKeyboard mService;
	private boolean mNoticing = false;
	private final ArrayList<CharSequence> mSuggestions = new ArrayList<CharSequence>();
	private boolean mShowingCompletions;
	private CharSequence mSelectedString;
	private CharSequence mJustAddedWord;
	private int mSelectedIndex;
	private int mTouchX = OUT_OF_BOUNDS_X_COORD;
	private final Drawable mSelectionHighlight;
	private boolean mTypedWordValid;

	private boolean mHaveMinimalSuggestion;

	private Rect mBgPadding;

	// private final TextView mPreviewText;
	// private final PopupWindow mPreviewPopup;
	// private int mCurrentWordIndex;
	private Drawable mDivider;

	private static final int MAX_SUGGESTIONS = 32;
	private static final int SCROLL_PIXELS = 20;

	private final int[] mWordWidth = new int[MAX_SUGGESTIONS];
	private final int[] mWordX = new int[MAX_SUGGESTIONS];
	// private int mPopupPreviewX;
	// private int mPopupPreviewY;

	// private static final int X_GAP = 30;
	private final float mXGap;
	private final int mColorNormal;
	private final int mColorRecommended;
	private final int mColorOther;
	private final Paint mPaint;
	// private final int mDescent;
	private boolean mScrolled;
	private boolean mShowingAddToDictionary;
	private CharSequence mAddToDictionaryHint;

	private int mTargetScrollX;

	private final int mMinTouchableWidth;

	private int mTotalWidth;

	private final GestureDetector mGestureDetector;

	public CandidateView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Construct a CandidateView for showing suggested words for completion.
	 * 
	 * @param context
	 * @param attrs
	 */
	public CandidateView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mSelectionHighlight = context.getResources().getDrawable(
				R.drawable.list_selector_background_pressed);

		mAddToDictionaryHint = context
				.getString(R.string.hint_add_to_dictionary);
		// themed
		final KeyboardTheme theme = KeyboardThemeFactory
				.getCurrentKeyboardTheme(context);
		TypedArray a = theme.getPackageContext().obtainStyledAttributes(attrs,
				R.styleable.AnySoftKeyboardTheme, 0, theme.getThemeResId());
		int colorNormal = context.getResources().getColor(
				R.color.candidate_normal);
		int colorRecommended = context.getResources().getColor(
				R.color.candidate_recommended);
		int colorOther = context.getResources().getColor(
				R.color.candidate_other);
		float fontSizePixel = context.getResources().getDimensionPixelSize(
				R.dimen.candidate_font_height);
		try {
			colorNormal = a.getColor(
					R.styleable.AnySoftKeyboardTheme_suggestionNormalTextColor,
					colorNormal);
			colorRecommended = a
					.getColor(
							R.styleable.AnySoftKeyboardTheme_suggestionRecommendedTextColor,
							colorRecommended);
			colorOther = a.getColor(
					R.styleable.AnySoftKeyboardTheme_suggestionOthersTextColor,
					colorOther);
			mDivider = a
					.getDrawable(R.styleable.AnySoftKeyboardTheme_suggestionDividerImage);
			final Drawable stripImage = a
					.getDrawable(R.styleable.AnySoftKeyboardTheme_suggestionBackgroundImage);
			if (stripImage == null)
				setBackgroundColor(Color.BLACK);
			else
				setBackgroundDrawable(stripImage);
			fontSizePixel = a.getDimension(
					R.styleable.AnySoftKeyboardTheme_suggestionTextSize,
					fontSizePixel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mXGap = a.getDimension(
				R.styleable.AnySoftKeyboardTheme_suggestionWordXGap, 20);
		a.recycle();
		mColorNormal = colorNormal;
		mColorRecommended = colorRecommended;
		mColorOther = colorOther;
		if (mDivider == null)
			mDivider = context.getResources().getDrawable(
					R.drawable.dark_suggestions_divider);
		// end of themed

		mPaint = new Paint();
		mPaint.setColor(mColorNormal);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(fontSizePixel);
		mPaint.setStrokeWidth(0);
		mPaint.setTextAlign(Align.CENTER);
		// mDescent = (int) mPaint.descent();
		mMinTouchableWidth = (int) context.getResources().getDimension(
				R.dimen.candidate_min_touchable_width);

		mGestureDetector = new GestureDetector(
				new CandidateStripGestureListener(mMinTouchableWidth));
		setWillNotDraw(false);
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
		scrollTo(0, getScrollY());
	}

	private class CandidateStripGestureListener extends
			GestureDetector.SimpleOnGestureListener {
		private final int mTouchSlopSquare;

		public CandidateStripGestureListener(int touchSlop) {
			// Slightly reluctant to scroll to be able to easily choose the
			// suggestion
			mTouchSlopSquare = touchSlop * touchSlop;
		}

		/*
		 * @Override public void onLongPress(MotionEvent me) { if
		 * (mSuggestions.size() > 0) { if (me.getX() + getScrollX() <
		 * mWordWidth[0] && getScrollX() < 10) { longPressFirstWord(); } } }
		 */
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

	/**
	 * A connection back to the service to communicate with the text field
	 * 
	 * @param listener
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

		final int dividerYOffest = (height - mDivider.getMinimumHeight()) / 2;
		final int count = mSuggestions.size();
		final Rect bgPadding = mBgPadding;
		final Paint paint = mPaint;
		final int touchX = mTouchX;
		final int scrollX = getScrollX();
		final boolean scrolled = mScrolled;
		final boolean typedWordValid = mTypedWordValid;

		// boolean existsAutoCompletion = false;

		int x = 0;
		for (int i = 0; i < count; i++) {
			CharSequence suggestion = mSuggestions.get(i);
			if (suggestion == null)
				continue;
			final int wordLength = suggestion.length();

			paint.setColor(mColorNormal);
			if (mHaveMinimalSuggestion
					&& ((i == 1 && !typedWordValid) || (i == 0 && typedWordValid))) {
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
				wordWidth = (int) (textWidth + mXGap * 2);
				mWordWidth[i] = wordWidth;
			}

			mWordX[i] = x;

			if (touchX != OUT_OF_BOUNDS_X_COORD && !scrolled
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
				if (AnyApplication.getConfig().workaround_alwaysUseDrawText()) {
					final int y = (int) (height + mPaint.getTextSize() - mPaint
							.descent()) / 2;
					canvas.drawText(suggestion, 0, wordLength, x + wordWidth
							/ 2, y, paint);
				} else {
					final int y = (int) (height - mPaint.getTextSize() + mPaint
							.descent()) / 2;
					// no matter what: StaticLayout
					float textX = x + (wordWidth / 2) - mXGap;
					float textY = y - bgPadding.bottom - bgPadding.top;

					canvas.translate(textX, textY);

					TextPaint suggestionPaint = new TextPaint(paint);
					StaticLayout suggestionText = new StaticLayout(suggestion,
							suggestionPaint, wordWidth, Alignment.ALIGN_CENTER,
							0.0f, 0.0f, false);
					suggestionText.draw(canvas);

					canvas.translate(-textX, -textY);
				}
				// (-)
				paint.setColor(mColorOther);
				canvas.translate(x + wordWidth, 0);
				// Draw a divider unless it's after the hint
				if (count > 1 && (!mShowingAddToDictionary)) {
					canvas.translate(0, dividerYOffest);
					mDivider.draw(canvas);
					canvas.translate(0, -dividerYOffest);
				}
				canvas.translate(-x - wordWidth, 0);
			}
			paint.setTypeface(Typeface.DEFAULT);
			x += wordWidth;
		}
		// mService.onAutoCompletionStateChanged(existsAutoCompletion);
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

	public void setSuggestions(List<CharSequence> suggestions,
			boolean completions, boolean typedWordValid,
			boolean haveMinimalSuggestion) {
		clear();
		if (suggestions != null) {
			int insertCount = Math.min(suggestions.size(), MAX_SUGGESTIONS);
			for (CharSequence suggestion : suggestions) {
				mSuggestions.add(suggestion);
				if (--insertCount == 0)
					break;
			}
		}
		mShowingCompletions = completions;
		mTypedWordValid = typedWordValid;
		scrollTo(0, getScrollY());
		mTargetScrollX = 0;
		mHaveMinimalSuggestion = haveMinimalSuggestion;
		// Compute the total width
		onDraw(null);
		invalidate();
		requestLayout();
	}

	public boolean isShowingAddToDictionaryHint() {
		return mShowingAddToDictionary;
	}

	public void showAddToDictionaryHint(CharSequence word) {
		ArrayList<CharSequence> suggestions = new ArrayList<CharSequence>();
		suggestions.add(word);
		suggestions.add(mAddToDictionaryHint);
		setSuggestions(suggestions, false, false, false);
		mShowingAddToDictionary = true;
	}

	public boolean dismissAddToDictionaryHint() {
		if (!mShowingAddToDictionary)
			return false;
		clear();
		return true;
	}

	/* package */List<CharSequence> getSuggestions() {
		return mSuggestions;
	}

	public void clear() {
		// Don't call mSuggestions.clear() because it's being used for logging
		// in LatinIME.pickSuggestionManually().
		mSuggestions.clear();
		mNoticing = false;
		mTouchX = OUT_OF_BOUNDS_X_COORD;
		mSelectedString = null;
		mSelectedIndex = -1;
		mShowingAddToDictionary = false;
		invalidate();
		Arrays.fill(mWordWidth, 0);
		Arrays.fill(mWordX, 0);
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {

		if (mGestureDetector.onTouchEvent(me)) {
			return true;
		}

		int action = me.getAction();
		final int x = (int) me.getX();
		final int y = (int) me.getY();
		mTouchX = x;

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			if (y <= 0) {
				// Fling up!?
				//Fling up should be a hacker's way to delete words (user dictionary words)
				if (mSelectedString != null) {
					Log.d(TAG, "Fling up from candidates view. Deleting word at index "+mSelectedIndex+", which is "+mSelectedString);
					mService.removeFromUserDictionary(mSelectedString.toString());
					clear();
				}
				/*
				if (mSelectedString != null) {
					// If there are completions from the application, we don't
					// change the state to
					// STATE_PICKED_SUGGESTION
					if (!mShowingCompletions) {
						// This "acceptedSuggestion" will not be counted as a
						// word because
						// it will be counted in pickSuggestion instead.
						TextEntryState.acceptedSuggestion(mSuggestions.get(0),
								mSelectedString);
					}
					mService.pickSuggestionManually(mSelectedIndex,
							mSelectedString);
					mSelectedString = null;
					mSelectedIndex = -1;
				}
				*/
			}
			break;
		case MotionEvent.ACTION_UP:
			if (!mScrolled) {
				if (mSelectedString != null) {
					if (mShowingAddToDictionary) {
						final CharSequence word = mSuggestions.get(0);
						if (word.length() >= 2 && !mNoticing) {
							Log.d(TAG, "User wants to add the word " + word + " to the user-dictionary.");
							mService.addWordToDictionary(word.toString());
						}
					} else if (!mNoticing) {
						if (!mShowingCompletions) {
							TextEntryState.acceptedSuggestion(
									mSuggestions.get(0), mSelectedString);
						}
						mService.pickSuggestionManually(mSelectedIndex,
								mSelectedString);
					} else /*if (mNoticing)*/ {
						if (action == MotionEvent.ACTION_UP && mSelectedIndex == 1
								&& !TextUtils.isEmpty(mJustAddedWord)) {
							// 1 is the index of "Remove?"
							Log.d(TAG, "User wants to remove an added word "
									+ mJustAddedWord);
							mService.removeFromUserDictionary(mJustAddedWord.toString());
						}
					}
				}
			}
			/*
			 * mSelectedString = null; mSelectedIndex = -1; requestLayout();
			 */
			// hidePreview();
			invalidate();
			break;
		}
		return true;
	}

	public void notifyAboutWordAdded(CharSequence word) {
		mJustAddedWord = word;
		ArrayList<CharSequence> notice = new ArrayList<CharSequence>(2);
		notice.add(getContext().getResources().getString(R.string.added_word,
				mJustAddedWord));
		notice.add(getContext().getResources().getString(
				R.string.revert_added_word_question));
		setSuggestions(notice, false, true, false);
		mNoticing = true;
	}
	
	public void notifyAboutRemovedWord(CharSequence word) {
		mJustAddedWord = null;
		ArrayList<CharSequence> notice = new ArrayList<CharSequence>(1);
		notice.add(getContext().getResources().getString(R.string.removed_word, word));
		setSuggestions(notice, false, true, false);
		mNoticing = true;
	}

	public void replaceTypedWord(CharSequence typedWord) {
		if (mSuggestions.size() > 0) {
			mSuggestions.set(0, typedWord);
			invalidate();
		}
	}
}